package cz.metacentrum.perun.voot;

import cz.metacentrum.perun.core.api.User;

/**
 * Class defines person with membership encoded according to the OpenSocial Social Data Specification.
 * Attributes that are not part of specification have namespace prefix 'voot_'.
 *
 * @author Martin Malik<374128@mail.muni.cz>
 * @see <a href="http://opensocial-resources.googlecode.com/svn/spec/2.0.1/Social-Data.xml#Person">Social-Data-Person</a>
 */
public class VOOTMember extends VOOTPerson{

	private String voot_membership_role;

	/**
	 * VOOTMember represents person with membership encoded according to the OpenSocial Social Data Specification using in VOOT protocol.
	 *
	 * @param user
	 * @param emails
	 * @param voot_membership_role
	 */
	public VOOTMember(User user, Email[] emails, String voot_membership_role){
		super(user, emails);
		this.voot_membership_role = voot_membership_role;
	}

	/**
	 * Return membership role of person, e.g. 'admin', 'member'.
	 *
	 * @return    membership role of person
	 */
	public String getVoot_membership_role() {
		return voot_membership_role;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 59 * hash + (this.voot_membership_role != null ? this.voot_membership_role.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final VOOTMember other = (VOOTMember) obj;
		if ((this.voot_membership_role == null) ? (other.voot_membership_role != null) : !this.voot_membership_role.equals(other.voot_membership_role)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(", voot_memberhip_role='" + voot_membership_role + "']");

		return sb.toString();
	}
}
