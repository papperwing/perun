package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Auditable;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * Owner of resources
 *
 * @author Michal Prochazka
 * @author Slavek Licehammer
 */
public class Owner extends Auditable {


	private String name = "";
	private String contact = "";
	private OwnerType type;

	public Owner() {
	}

	public Owner(int id, String name, String contact) {
		super(id);
		this.name = name;
		this.contact = contact;
	}

	public Owner(int id, String name, String contact, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.name = name;
		this.contact = contact;
	}

	public Owner(int id, String name, String contact, OwnerType type) {
		this(id, name, contact);
		this.type = type;
	}

	public Owner(int id, String name, String contact, OwnerType type, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.name = name;
		this.contact = contact;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	/**
	 * Gets the contact for this instance.
	 *
	 * @return The contact.
	 */
	public String getContact()
	{
		return this.contact;
	}

	/**
	 * Sets the contact for this instance.
	 *
	 * @param contact The contact.
	 */
	public void setContact(String contact)
	{
		this.contact = contact;
	}

	public OwnerType getType() {
		return this.type;
	}

	public void setType(OwnerType type) {
		this.type = type;
	}

	public void setTypeByString(String type) {
		if(type == null) this.type = null;
		else this.type = OwnerType.valueOf(type);
	}

	@Override
	public String serializeToString() {
		return this.getClass().getSimpleName() +":[" +
			"id=<" + getId() + ">" +
			", name=<" + (getName() == null ? "\\0" : BeansUtils.createEscaping(getName())) + ">" +
			", contact=<" + (getContact() == null ? "\\0" : BeansUtils.createEscaping(getContact())) + ">" +
			", type=<" + (getType() == null ? "\\0" : BeansUtils.createEscaping(getType().toString())) + ">" +
			']';
	}

	public String toString() {
		return getClass().getSimpleName() + ":[" +
			"id='" + getId() + '\'' +
			", name='" + name + '\'' +
			", contact='" + contact + '\'' +
			", type='" + type +
			"']";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contact == null) ? 0 : contact.hashCode());
		result = prime * result + getId();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Owner other = (Owner) obj;
		if (contact == null) {
			if (other.contact != null) {
				return false;
			}
		} else if (!contact.equals(other.contact)) {
			return false;
		}
		if (getId() != other.getId()) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}


}
