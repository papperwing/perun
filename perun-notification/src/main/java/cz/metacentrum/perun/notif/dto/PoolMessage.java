package cz.metacentrum.perun.notif.dto;

import cz.metacentrum.perun.notif.entities.PerunNotifPoolMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Holds PerunNotifPoolMessages aggregated for user and templateId, and same keyAttributes
 * @author tomas.tunkl
 *
 */
public class PoolMessage {

	/**
	 * Template id common for all PerunNotifPoolMessages from list
	 */
	private Integer templateId;

	/**
	 * KeyAttributes which are the same for PerunNotifPoolMessages from list
	 */
	private Map<String, String> keyAttributes;

	/**
	 * PerunNotifPoolMessages from db, which has common keyAttributes and templateIds
	 */
	private List<PerunNotifPoolMessage> list;

	/**
	 * Locale common for all messages, used for main language of result message
	 */
	private Locale locale;

	public Integer getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}

	public Map<String, String> getKeyAttributes() {
		return keyAttributes;
	}

	public void setKeyAttributes(Map<String, String> keyAttributes) {
		this.keyAttributes = keyAttributes;
	}

	public List<PerunNotifPoolMessage> getList() {
		return list;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void addToList(PerunNotifPoolMessage message) {
		if (list == null) {
			list = new ArrayList<PerunNotifPoolMessage>();
		}

		list.add(message);
	}
}
