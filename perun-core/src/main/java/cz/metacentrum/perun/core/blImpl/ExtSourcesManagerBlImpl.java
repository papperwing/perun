package cz.metacentrum.perun.core.blImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.CandidateNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.bl.ExtSourcesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.ExtSourcesManagerImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import cz.metacentrum.perun.core.implApi.ExtSourcesManagerImplApi;

public class ExtSourcesManagerBlImpl implements ExtSourcesManagerBl {
	final static Logger log = LoggerFactory.getLogger(ExtSourcesManagerBlImpl.class);

	private final ExtSourcesManagerImplApi extSourcesManagerImpl;
	private PerunBl perunBl;
	private AtomicBoolean initialized = new AtomicBoolean(false);


	public ExtSourcesManagerBlImpl(ExtSourcesManagerImplApi extSourcesManagerImpl) {
		this.extSourcesManagerImpl = extSourcesManagerImpl;
	}

	public void initialize(PerunSession sess) {
		if (!this.initialized.compareAndSet(false, true)) return;
		this.extSourcesManagerImpl.initialize(sess);
	}

	public ExtSource createExtSource(PerunSession sess, ExtSource extSource) throws InternalErrorException, ExtSourceExistsException {
		getPerunBl().getAuditer().log(sess, "{} created.", extSource);
		return getExtSourcesManagerImpl().createExtSource(sess, extSource);
	}

	public void deleteExtSource(PerunSession sess, ExtSource extSource) throws InternalErrorException, ExtSourceAlreadyRemovedException {
		getExtSourcesManagerImpl().deleteExtSource(sess, extSource);
		getPerunBl().getAuditer().log(sess, "{} deleted.", extSource);
	}

	public ExtSource getExtSourceById(PerunSession sess, int id) throws InternalErrorException, ExtSourceNotExistsException {
		return getExtSourcesManagerImpl().getExtSourceById(sess, id);
	}

	public ExtSource getExtSourceByName(PerunSession sess, String name) throws InternalErrorException, ExtSourceNotExistsException {
		return getExtSourcesManagerImpl().getExtSourceByName(sess, name);
	}

	public List<ExtSource> getVoExtSources(PerunSession sess, Vo vo) throws InternalErrorException {
		return getExtSourcesManagerImpl().getVoExtSources(sess, vo);
	}

	public List<ExtSource> getExtSources(PerunSession sess) throws InternalErrorException {
		return getExtSourcesManagerImpl().getExtSources(sess);
	}
	public void addExtSource(PerunSession sess, Vo vo, ExtSource source) throws InternalErrorException, ExtSourceAlreadyAssignedException {
		getExtSourcesManagerImpl().addExtSource(sess, vo, source);
		getPerunBl().getAuditer().log(sess, "{} added to {}.", source, vo);
	}

	public ExtSource checkOrCreateExtSource(PerunSession sess, String extSourceName, String extSourceType) throws InternalErrorException {
		// Check if the extSource exists
		try {
			return getExtSourcesManagerImpl().getExtSourceByName(sess, extSourceName);
		} catch (ExtSourceNotExistsException e) {
			// extSource doesn't exist, so create new one
			ExtSource extSource = new ExtSource();
			extSource.setName(extSourceName);
			extSource.setType(extSourceType);
			try {
				return this.createExtSource(sess, extSource);
			} catch (ExtSourceExistsException e1) {
				throw new ConsistencyErrorException("Creating existing extSource", e1);
			}
		}
	}

	public void removeExtSource(PerunSession sess, Vo vo, ExtSource source) throws InternalErrorException, ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException {
		getExtSourcesManagerImpl().removeExtSource(sess, vo, source);
		getPerunBl().getAuditer().log(sess, "{} removed from {}.", source, vo);
	}

	public List<User> getInvalidUsers(PerunSession sess, ExtSource source) throws InternalErrorException {
		List<Integer> usersIds;
		List<User> invalidUsers = new ArrayList<User>();

		// Get all users, who are associated with this extSource
		usersIds = getExtSourcesManagerImpl().getAssociatedUsersIdsWithExtSource(sess, source);
		List<User> users = getPerunBl().getUsersManagerBl().getUsersByIds(sess, usersIds);

		for (User user: users) {
			// From user's userExtSources get the login
			String userLogin = "";

			List<UserExtSource> userExtSources = getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
			for (UserExtSource userExtSource: userExtSources) {
				if (userExtSource.getExtSource().equals(this)) {
					// It is enough to have at least one login from the extSource
					// TODO jak budeme kontrolovat, ze mu zmizel jeden login a zustal jiny, zajima nas to?
					userLogin = userExtSource.getLogin();
				}
			}

			// Check if the login is still present in the extSource
			try {
				((ExtSourceApi) source).getSubjectByLogin(userLogin);
			} catch (SubjectNotExistsException e) {
				invalidUsers.add(user);
			} catch (ExtSourceUnsupportedOperationException e) {
				log.warn("ExtSource {} doesn't support getSubjectByLogin", source.getName());
				continue;
			}
		}

		return invalidUsers;
	}

	/**
	 * Gets the extSourcesManagerImpl for this instance.
	 *
	 * @return extSourceManagerImpl
	 */
	public ExtSourcesManagerImplApi getExtSourcesManagerImpl() {
		return this.extSourcesManagerImpl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	public void checkExtSourceExists(PerunSession sess, ExtSource extSource) throws InternalErrorException, ExtSourceNotExistsException {
		getExtSourcesManagerImpl().checkExtSourceExists(sess, extSource);
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	@Override
	public Candidate getCandidate(PerunSession sess, ExtSource source, String login) throws InternalErrorException, ExtSourceNotExistsException, CandidateNotExistsException, ExtSourceUnsupportedOperationException  {
		// New Canddate
		Candidate candidate = new Candidate();

		// Prepare userExtSource object
		UserExtSource userExtSource = new UserExtSource();
		userExtSource.setExtSource(source);
		userExtSource.setLogin(login);

		// Set the userExtSource
		candidate.setUserExtSource(userExtSource);

		// Get the subject from the extSource
		Map<String, String> subject = null;
		try {
			subject = ((ExtSourceApi) source).getSubjectByLogin(login);
		} catch (SubjectNotExistsException e) {
			throw new CandidateNotExistsException(login);
		}

		if (subject == null) {
			throw new InternalErrorException("Candidate with login [" + login + "] not exists");
		}

		candidate.setFirstName(subject.get("firstName"));
		candidate.setLastName(subject.get("lastName"));
		candidate.setMiddleName(subject.get("middleName"));
		candidate.setTitleAfter(subject.get("titleAfter"));
		candidate.setTitleBefore(subject.get("titleBefore"));

		// Additional userExtSources
		List<UserExtSource> additionalUserExtSources = new ArrayList<UserExtSource>();

		// Filter attributes
		Map<String, String> attributes = new HashMap<String, String>();
		for (String attrName: subject.keySet()) {
			// Allow only users and members attributes
			// FIXME volat metody z attributesManagera nez kontrolovat na zacatek jmena
			if (attrName.startsWith(AttributesManager.NS_MEMBER_ATTR) || attrName.startsWith(AttributesManager.NS_USER_ATTR)) {
				attributes.put(attrName, subject.get(attrName));
			} else if (attrName.startsWith(ExtSourcesManagerImpl.USEREXTSOURCEMAPPING)) {
				// Add additionalUserExtSources
				String[] userExtSourceRaw =  subject.get(attrName).split("\\|"); // Entry contains extSourceName|extSourceType|extLogin[|LoA]
				log.debug("Processing additionalUserExtSource {}",  subject.get(attrName));

				String additionalExtSourceName = userExtSourceRaw[0];
				String additionalExtSourceType = userExtSourceRaw[1];
				String additionalExtLogin = userExtSourceRaw[2];
				int additionalExtLoa = -1;
				if (userExtSourceRaw[3] != null) {
					try {
						additionalExtLoa = Integer.parseInt(userExtSourceRaw[3]);
					} catch (NumberFormatException e) {
						throw new InternalErrorException("Candidate with login [" + login + "] has wrong LoA '" + userExtSourceRaw[3] + "'.");
					}
				}

				ExtSource additionalExtSource;

				if (additionalExtSourceName == null || additionalExtSourceName.isEmpty() ||
						additionalExtSourceType == null || additionalExtSourceType.isEmpty() ||
						additionalExtLogin == null || additionalExtLogin.isEmpty()) {
					log.error("User with login {} has invalid additional userExtSource defined {}.", login, userExtSourceRaw);
				} else {
					try {
						// Try to get extSource, with full extSource object (containg ID)
						additionalExtSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, additionalExtSourceName);
					} catch (ExtSourceNotExistsException e) {
						try {
							// Create new one if not exists
							additionalExtSource = new ExtSource(additionalExtSourceName, additionalExtSourceType);
							additionalExtSource = getPerunBl().getExtSourcesManagerBl().createExtSource(sess, additionalExtSource);
						} catch (ExtSourceExistsException e1) {
							throw new ConsistencyErrorException("Creating existin extSource: " + additionalExtSourceName);
						}
					}
					if (additionalExtLoa != -1) {
						additionalUserExtSources.add(new UserExtSource(additionalExtSource, additionalExtLoa, additionalExtLogin));
					} else {
						additionalUserExtSources.add(new UserExtSource(additionalExtSource, additionalExtLogin));
					}
				}
			}
		}

		candidate.setAdditionalUserExtSources(additionalUserExtSources);
		candidate.setAttributes(attributes);

		return candidate;
	}

	public void loadExtSourcesDefinitions(PerunSession sess) {
		getExtSourcesManagerImpl().loadExtSourcesDefinitions(sess);
	}
}
