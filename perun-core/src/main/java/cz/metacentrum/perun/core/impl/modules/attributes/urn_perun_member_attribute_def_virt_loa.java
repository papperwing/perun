package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberVirtualAttributesModuleImplApi;

/**
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_member_attribute_def_virt_loa extends MemberVirtualAttributesModuleAbstract implements MemberVirtualAttributesModuleImplApi {

	public void checkAttributeValue(PerunSessionImpl sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		if(!attribute.equals(getAttributeValue(sess, member, attribute))) throw new WrongAttributeValueException(attribute, member, "Attribute value is not the highest value from member's UserExtSources Loas.");
	}

	public Attribute fillAttribute(PerunSessionImpl sess, Member member, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		return new Attribute(attributeDefinition);
	}

	public Attribute getAttributeValue(PerunSessionImpl sess, Member member, AttributeDefinition attributeDefinition) throws InternalErrorException {
		User user = null;
		try {
			user = sess.getPerunBl().getUsersManagerBl().getUserById(sess, member.getUserId());
		} catch(UserNotExistsException ex) {
			throw new InternalErrorException("There is no user for get member "+ member,ex);
		}
		List<UserExtSource> extSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
		Integer maxLoa = 0;
		for(UserExtSource e : extSources) {
			if(maxLoa < e.getLoa()) maxLoa = e.getLoa();
		}
		Attribute attribute = new Attribute(attributeDefinition);
		attribute.setValue(maxLoa.toString());
		return attribute;
	}

	public boolean setAttributeValue(PerunSessionImpl sess, Member member, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		//No need to set, its same like check for this module, and check is used after every set
		return false;
	}

	public void removeAttributeValue(PerunSessionImpl sess, Member member, AttributeDefinition attributeDefinition) throws InternalErrorException {
		//Not suported yet.
		throw new InternalErrorException("Can't remove value of this virtual attribute this way. " + attributeDefinition);
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_VIRT);
		attr.setFriendlyName("loa");
		attr.setType(Integer.class.getName());
		attr.setDescription("The highest value of LoA from all member's userExtSources.");
		return attr;
	}

}
