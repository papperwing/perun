package cz.metacentrum.perun.core.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import cz.metacentrum.perun.core.api.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.impl.Utils;

/**
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class MembersManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private static final String MEMBERS_MANAGER_ENTRY = "MembersManagerEntry";
	private Vo createdVo = null;
	final ExtSource extSource = new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
	private Group createdGroup;
	private Candidate candidate;
	private UserExtSource ues;
	private Member createdMember;
	private MembersManager membersManagerEntry;
	private AttributesManager attributesManagerEntry;
	private GroupsManager groupsManagerEntry;
	private UsersManager usersManagerEntry;

	@Before
	public void setUp() throws Exception {

		usersManagerEntry = perun.getUsersManager();
		attributesManagerEntry = perun.getAttributesManager();
		membersManagerEntry = perun.getMembersManager();
		final Vo vo = new Vo(0, "m3mb3r r00m", "m3mber-room");
		VosManager vosManagerEntry = perun.getVosManager();//new VosManagerEntry(perun);
		createdVo = vosManagerEntry.createVo(sess, vo);
		assertNotNull(createdVo);

		groupsManagerEntry = perun.getGroupsManager();
		final Group group = new Group("Test_Group_123456", "TestGroupDescr");
		createdGroup = groupsManagerEntry.createGroup(sess, createdVo, group);

		String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));              // his login in external source

		candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		ues = new UserExtSource(new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal"), extLogin);
		candidate.setUserExtSource(ues);
		candidate.setAttributes(new HashMap<String,String>());

		createdMember = perun.getMembersManagerBl().createMemberSync(sess, createdVo, candidate);
		assertNotNull("No member created", createdMember);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, createdMember));
		// save user for deletion after test

	}

	@Test
	public void createMember() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".createMemberSync()");

		final Member m;
		//createdMember should be initialized in setUp method. if not, do it on my own
		m = (createdMember != null)   ? createdMember : membersManagerEntry.createMember(sess,
				createdVo, candidate);

		assertNotNull(m);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, m));
		// save user for deletion after test
		assertTrue(m.getId() > 0);
	}

	@Test (expected=VoNotExistsException.class)
		public void createMemeberWhenVoNotExists() throws Exception {
			System.out.println(MEMBERS_MANAGER_ENTRY + ".createMemberSyncWhenVoNotExists()");

			membersManagerEntry.createMember(sess, new Vo(), candidate);

		}

	@Test (expected=AlreadyMemberException.class)
		public void createMemeberWhenAlreadyMember() throws Exception {
			System.out.println(MEMBERS_MANAGER_ENTRY + ".createMemberSyncWhenAlreadyMember()");

			membersManagerEntry.createMember(sess, createdVo, candidate);
			// shouldn't add member twice

		}

	@Test
	public void getMemberById() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".getMemberById()");


		final Member m = membersManagerEntry.getMemberById(sess,
				createdMember.getId());

		assertNotNull("unable to get member",m);
		assertEquals("returned member is not same as stored",createdMember.getId(), m.getId());

	}

	@Test (expected=MemberNotExistsException.class)
		public void getMemberByIdWhenMemberNotExists() throws Exception {
			System.out.println(MEMBERS_MANAGER_ENTRY + ".getMemberByIdWhenMemberNotExists()");

			membersManagerEntry.getMemberById(sess, 0);
			// shouldn't find member with ID 0

		}

	@Test
	public void getMemberByExtAuth() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".getMemberByExtAuth()");

		final Member expectedMember = membersManagerEntry.getMemberByUserExtSource(sess, createdVo, ues);
		assertNotNull("unable to return member by Ext auth",expectedMember);
		assertEquals("created and returned member is not the same",createdMember, expectedMember);

	}

	@Test (expected=VoNotExistsException.class)
		public void getMemberByExtAuthWhenVoNotExists() throws Exception {
			System.out.println(MEMBERS_MANAGER_ENTRY + ".getMemberByExtAuthWhenVoNotExists()");

			membersManagerEntry.getMemberByUserExtSource(sess, new Vo(), ues);
			// shouldn't find VO

		}

	@Test (expected=MemberNotExistsException.class)
		public void getMemberByExtAuthWhenMemberNotExists() throws Exception {
			System.out.println(MEMBERS_MANAGER_ENTRY + ".getMemberByExtAuthWhenMemberNotExists()");

			final UserExtSource ues2 = ues;
			ues2.setLogin("neexistuje");
			membersManagerEntry.getMemberByUserExtSource(sess, createdVo, ues2);
			// shouldn't find Member

		}

	@Test
	public void getMemberByUser() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".getMemberByUser()");

		final User u = perun.getUsersManager().getUserByUserExtSource(sess, ues);

		final Member expectedMember = membersManagerEntry.getMemberByUser(sess, createdVo, u);
		assertNotNull(expectedMember);
		assertEquals(createdMember, expectedMember);

	}

	@Test (expected=VoNotExistsException.class)
		public void getMemberByUserWhenVoNotExists() throws Exception {
			System.out.println(MEMBERS_MANAGER_ENTRY + ".getMemberByUserWhenVoNotExists()");

			final User u = perun.getUsersManager().getUserByUserExtSource(sess, ues);

			membersManagerEntry.getMemberByUser(sess, new Vo(), u);
			// shouldn't find VO

		}

	@Test (expected=MemberNotExistsException.class)
		public void getMemberByUserWhenMemberNotExists() throws Exception {
			System.out.println(MEMBERS_MANAGER_ENTRY + ".getMemberByUserWhenMemberNotExists()");

			final User u = perun.getUsersManager().getUserByUserExtSource(sess, ues);
			membersManagerEntry.deleteMember(sess, createdMember);
			membersManagerEntry.getMemberByUser(sess, createdVo, u);
			// shouldn't find member

		}

	@Test (expected=UserNotExistsException.class)
		public void getMemberByUserWhenUserNotExists() throws Exception {
			System.out.println(MEMBERS_MANAGER_ENTRY + ".getMemberByUserWhenUserNotExists()");

			final User u = perun.getUsersManager().getUserByUserExtSource(sess, ues);
			u.setId(0);
			membersManagerEntry.getMemberByUser(sess, createdVo, u);
			// shouldn't find user

		}

	@Test
	public void getMemberVo() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".getMemberVo()");

		Vo vo = membersManagerEntry.getMemberVo(sess, createdMember);
		assertNotNull("unable to get VO by member",vo);
		assertEquals("saved and returned member's Vo is not the same",vo , createdVo);

	}

	@Test (expected=MemberNotExistsException.class)
		public void getMemberVoWhenMemberNotExists() throws Exception {
			System.out.println(MEMBERS_MANAGER_ENTRY + ".getMemberVoWhenMemberNotExists()");

			membersManagerEntry.getMemberVo(sess, new Member());
			// shouldn't find Member

		}

	@Test
	public void getMembersCount() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".getMembersCount()");

		final int count = membersManagerEntry.getMembersCount(sess, createdVo);
		assertTrue("testing VO should have only 1 member", count == 1);

	}

	@Test (expected=VoNotExistsException.class)
		public void getMembersCountWhenVoNotExists() throws Exception {
			System.out.println(MEMBERS_MANAGER_ENTRY + ".getMembersCountWhenVoNotExists()");

			membersManagerEntry.getMembersCount(sess, new Vo());
			// shouldn't find VO

		}

	@Test
	public void getMembersCountByStatus() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".getMembersCountByStatus()");

		final int count = membersManagerEntry.getMembersCount(sess, createdVo, Status.SUSPENDED);
		assertTrue("testing VO should have 0 members with SUSPENDED status", count == 0);
		final int count2 = membersManagerEntry.getMembersCount(sess, createdVo, Status.VALID);
		assertTrue("testing VO should have 1 member with VALID status", count2 == 1);

	}

	@Test
	public void getMembers() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".getMembers()");

		List<Member> members = membersManagerEntry.getMembers(sess, createdVo);
		assertTrue("should return only 1 member",members.size() == 1);
		assertTrue("should return our member",members.contains(createdMember));

	}

	@Test (expected=VoNotExistsException.class)
		public void getMembersWhenVoNotExists() throws Exception {
			System.out.println(MEMBERS_MANAGER_ENTRY + ".getMembersWhenVoNotExists()");

			membersManagerEntry.getMembers(sess, new Vo());
			// shouldn't find VO

		}

	@Test
	public void getMembersByUser() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".getMembersByUser()");

		final User u = perun.getUsersManager().getUserByMember(sess, createdMember);
		List<Member> members = membersManagerEntry.getMembersByUser(sess, u);
		assertNotNull("unable to return members by user",members);
		assertTrue("should return 1 member by user",members.contains(createdMember));

	}

	@Test (expected=UserNotExistsException.class)
		public void getMembersByUserWhenUserNotExists() throws Exception {
			System.out.println(MEMBERS_MANAGER_ENTRY + ".getMembersByUserWhenUserNotExists()");

			final User u = perun.getUsersManager().getUserByMember(sess, createdMember);
			u.setId(0);
			membersManagerEntry.getMembersByUser(sess, u);
			// shouldn't find user

		}

	@Test(expected=MemberNotExistsException.class)
	public void deleteMember() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".deleteMember()");

		//ensure that the test-member already exists..
		assertNotNull(membersManagerEntry.getMemberById(sess, createdMember.getId()));
		membersManagerEntry.deleteMember(sess, createdMember);

		membersManagerEntry.getMemberById(sess, createdMember.getId());

	}

	@Test(expected=MemberNotExistsException.class)
	public void deleteMemberWhenMemberNotExists() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".deleteMemberWhenMemberNotExists()");

		membersManagerEntry.deleteMember(sess, new Member());

	}

	@Test(expected=MemberNotExistsException.class)
	public void deleteAllMembers() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".deleteAllMembers()");

		//ensure that the test-member already exists..
		assertNotNull(membersManagerEntry.getMemberById(sess, createdMember.getId()));
		membersManagerEntry.deleteAllMembers(sess, createdVo);
		membersManagerEntry.getMemberById(sess, createdMember.getId());

	}

	@Test(expected=VoNotExistsException.class)
	public void deleteAllMembersWhenVoNotExists() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".deleteAllMembersWhenVoNotExists()");

		membersManagerEntry.deleteAllMembers(sess, new Vo());

	}

	@Test
	public void extendMembershipToParticularDate() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".extendMembershipToParticularDate()");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<String, String>();
		extendMembershipRules.put(MembersManager.membershipPeriodKeyName, "1.1.");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Try to extend membership
		membersManagerEntry.extendMembership(sess, createdMember);

		Attribute membershipAttribute = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		Date extendedDate = BeansUtils.DATE_FORMATTER.parse((String) membershipAttribute.getValue());
		Calendar extendedCalendar = Calendar.getInstance();
		extendedCalendar.setTime(extendedDate);

		// Set to 1.1. next year
		Calendar requiredCalendar = Calendar.getInstance();
		requiredCalendar.set(Calendar.MONTH, 0);
		requiredCalendar.set(Calendar.DAY_OF_MONTH, 1);
		requiredCalendar.add(Calendar.YEAR, 1);

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());
		assertEquals("Year must match", requiredCalendar.get(Calendar.YEAR), extendedCalendar.get(Calendar.YEAR));
		assertEquals("Month must match", requiredCalendar.get(Calendar.MONTH), extendedCalendar.get(Calendar.MONTH));
		assertEquals("Day must match", requiredCalendar.get(Calendar.DAY_OF_MONTH), extendedCalendar.get(Calendar.DAY_OF_MONTH));
	}

	@Test
	public void extendMembershipBy10Days() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".extendMembershipBy10Days()");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<String, String>();
		extendMembershipRules.put(MembersManager.membershipPeriodKeyName, "+10d");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Try to extend membership
		membersManagerEntry.extendMembership(sess, createdMember);

		Attribute membershipAttribute = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		Date extendedDate = BeansUtils.DATE_FORMATTER.parse((String) membershipAttribute.getValue());
		Calendar extendedCalendar = Calendar.getInstance();
		extendedCalendar.setTime(extendedDate);

		Calendar requiredCalendar = Calendar.getInstance();
		requiredCalendar.add(Calendar.DAY_OF_MONTH, 10); // Add 10 days to today

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());
		assertEquals("Year must match", requiredCalendar.get(Calendar.YEAR), extendedCalendar.get(Calendar.YEAR));
		assertEquals("Month must match", requiredCalendar.get(Calendar.MONTH), extendedCalendar.get(Calendar.MONTH));
		assertEquals("Day must match", requiredCalendar.get(Calendar.DAY_OF_MONTH), extendedCalendar.get(Calendar.DAY_OF_MONTH));
	}

	@Test
	public void extendMembershipInGracePeriod() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".extendMembershipInGracePeriod()");

		// Period will be set to the next day
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH)+1;

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<String, String>();
		// Set perid to day after today
		extendMembershipRules.put(MembersManager.membershipPeriodKeyName, day + "." + month + ".");
		extendMembershipRules.put(MembersManager.membershipGracePeriodKeyName, "1m");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Try to extend membership
		membersManagerEntry.extendMembership(sess, createdMember);

		Attribute membershipAttribute = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		Date extendedDate = BeansUtils.DATE_FORMATTER.parse((String) membershipAttribute.getValue());
		Calendar extendedCalendar = Calendar.getInstance();
		extendedCalendar.setTime(extendedDate);

		Calendar requiredCalendar = Calendar.getInstance();
		requiredCalendar.add(Calendar.DAY_OF_MONTH, 1);
		requiredCalendar.add(Calendar.YEAR, 1);

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());
		assertEquals("Year must match", requiredCalendar.get(Calendar.YEAR), extendedCalendar.get(Calendar.YEAR));
		assertEquals("Month must match", requiredCalendar.get(Calendar.MONTH), extendedCalendar.get(Calendar.MONTH));
		assertEquals("Day must match", requiredCalendar.get(Calendar.DAY_OF_MONTH), extendedCalendar.get(Calendar.DAY_OF_MONTH));
	}

	@Test
	public void extendMembershipOutsideGracePeriod() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".extendMembershipOutsideGracePeriod()");

		// Set period to three months later
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, 3);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH)+1;


		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<String, String>();
		// Set perid to day after today
		extendMembershipRules.put(MembersManager.membershipPeriodKeyName, day + "." + month + ".");
		extendMembershipRules.put(MembersManager.membershipGracePeriodKeyName, "1m");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Try to extend membership
		membersManagerEntry.extendMembership(sess, createdMember);

		Attribute membershipAttribute = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		Date extendedDate = BeansUtils.DATE_FORMATTER.parse((String) membershipAttribute.getValue());
		Calendar extendedCalendar = Calendar.getInstance();
		extendedCalendar.setTime(extendedDate);

		Calendar requiredCalendar = Calendar.getInstance();
		requiredCalendar.add(Calendar.MONTH, 3);

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());
		assertEquals("Year must match", requiredCalendar.get(Calendar.YEAR), extendedCalendar.get(Calendar.YEAR));
		assertEquals("Month must match", requiredCalendar.get(Calendar.MONTH), extendedCalendar.get(Calendar.MONTH));
		assertEquals("Day must match", requiredCalendar.get(Calendar.DAY_OF_MONTH), extendedCalendar.get(Calendar.DAY_OF_MONTH));
	}

	@Test
	public void extendMembershipForMemberWithSufficientLoa() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".extendMembershipForMemberWithSufficientLoa()");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<String, String>();
		extendMembershipRules.put(MembersManager.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(MembersManager.membershipDoNotExtendLoaKeyName, "0,1");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Set LOA 2 for member
		ExtSource es = perun.getExtSourcesManagerBl().getExtSourceByName(sess, "INTERNAL");
		ues = new UserExtSource(es, "abc");
		ues.setLoa(2);

		User user = usersManagerEntry.getUserByMember(sess, createdMember);
		usersManagerEntry.addUserExtSource(sess, user, ues);

		// Try to extend membership
		membersManagerEntry.extendMembership(sess, createdMember);

		Attribute membershipAttribute = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		Date extendedDate = BeansUtils.DATE_FORMATTER.parse((String) membershipAttribute.getValue());
		Calendar extendedCalendar = Calendar.getInstance();
		extendedCalendar.setTime(extendedDate);

		// Set to 1.1. next year
		Calendar requiredCalendar = Calendar.getInstance();
		requiredCalendar.set(Calendar.MONTH, 0);
		requiredCalendar.set(Calendar.DAY_OF_MONTH, 1);
		requiredCalendar.add(Calendar.YEAR, 1);

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());
		assertEquals("Year must match", requiredCalendar.get(Calendar.YEAR), extendedCalendar.get(Calendar.YEAR));
		assertEquals("Month must match", requiredCalendar.get(Calendar.MONTH), extendedCalendar.get(Calendar.MONTH));
		assertEquals("Day must match", requiredCalendar.get(Calendar.DAY_OF_MONTH), extendedCalendar.get(Calendar.DAY_OF_MONTH));
	}

	@Test
	public void extendMembershipForMemberWithInsufficientLoa() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".extendMembershipForMemberWithInsufficientLoa()");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<String, String>();
		extendMembershipRules.put(MembersManager.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(MembersManager.membershipDoNotExtendLoaKeyName, "0,1");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		Attribute membershipExpirationAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_ATTR_DEF+":membershipExpiration"));
		Calendar nowCalendar = Calendar.getInstance();
		membershipExpirationAttribute.setValue(BeansUtils.DATE_FORMATTER.format(nowCalendar.getTime()));
		attributesManagerEntry.setAttribute(sess, createdMember, membershipExpirationAttribute);

		// Set LOA 1 for member
		ExtSource es = perun.getExtSourcesManagerBl().getExtSourceByName(sess, "INTERNAL");
		ues = new UserExtSource(es, "abc");
		ues.setLoa(1);

		User user = usersManagerEntry.getUserByMember(sess, createdMember);
		usersManagerEntry.addUserExtSource(sess, user, ues);

		// Try to extend membership
		try {
			membersManagerEntry.extendMembership(sess, createdMember);
		} catch (ExtendMembershipException e) {
			assertTrue(e.getReason().equals(ExtendMembershipException.Reason.INSUFFICIENTLOA));
		}

		Attribute membershipAttribute = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertEquals("membership attribute value must contains same value as before extension.",
				BeansUtils.DATE_FORMATTER.format(nowCalendar.getTime()), membershipAttribute.getValue()); // Attribute cannot contain any value
	}

	@Test
	public void extendMembershipForDefinedLoaAllowed() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".extendMembershipForDefinedLoaAllowed()");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<String, String>();
		extendMembershipRules.put(MembersManager.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(MembersManager.membershipDoNotExtendLoaKeyName, "0");
		extendMembershipRules.put(MembersManager.membershipPeriodLoaKeyName, "1|+1m");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Set LOA 1 for member
		ExtSource es = perun.getExtSourcesManagerBl().getExtSourceByName(sess, "INTERNAL");
		ues = new UserExtSource(es, "abc");
		ues.setLoa(1);

		User user = usersManagerEntry.getUserByMember(sess, createdMember);
		usersManagerEntry.addUserExtSource(sess, user, ues);

		// Try to extend membership
		membersManagerEntry.extendMembership(sess, createdMember);

		Attribute membershipAttribute = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());

		// Try to extend membership once again
		membersManagerEntry.extendMembership(sess, createdMember);

		membershipAttribute = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		Date extendedDate = BeansUtils.DATE_FORMATTER.parse((String) membershipAttribute.getValue());
		Calendar extendedCalendar = Calendar.getInstance();
		extendedCalendar.setTime(extendedDate);

		Calendar requiredCalendar = Calendar.getInstance();
		requiredCalendar.add(Calendar.MONTH, 1);

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());
		assertEquals("Year must match", requiredCalendar.get(Calendar.YEAR), extendedCalendar.get(Calendar.YEAR));
		assertEquals("Month must match", requiredCalendar.get(Calendar.MONTH), extendedCalendar.get(Calendar.MONTH));
		assertEquals("Day must match", requiredCalendar.get(Calendar.DAY_OF_MONTH), extendedCalendar.get(Calendar.DAY_OF_MONTH));
	}

	@Test
	public void canExtendMembershipForDefinedLoaNotAllowed() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".extendMembershipForDefinedLoaNotAllowed()");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<String, String>();
		extendMembershipRules.put(MembersManager.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(MembersManager.membershipDoNotExtendLoaKeyName, "0");
		extendMembershipRules.put(MembersManager.membershipPeriodLoaKeyName, "1|+1m.");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Set LOA 1 for member
		ExtSource es = perun.getExtSourcesManagerBl().getExtSourceByName(sess, "INTERNAL");
		ues = new UserExtSource(es, "abc");
		ues.setLoa(0);

		User user = usersManagerEntry.getUserByMember(sess, createdMember);
		usersManagerEntry.addUserExtSource(sess, user, ues);

		// Try to extend membership
		membersManagerEntry.extendMembership(sess, createdMember);

		Attribute membershipAttributeFirst = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttributeFirst);
		assertNotNull("membership attribute value must be set", membershipAttributeFirst.getValue());

		// Try to extend membership
		assertFalse(membersManagerEntry.canExtendMembership(sess, createdMember));
	}

	@Test
	// It extend membership and try to extend it again, it must decline another expiration
	public void canExtendMembershipInGracePeriod() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".canExtendMembershipInGracePeriod()");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<String, String>();
		extendMembershipRules.put(MembersManager.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(MembersManager.membershipDoNotExtendLoaKeyName, "0");
		extendMembershipRules.put(MembersManager.membershipPeriodLoaKeyName, "1|+1m.");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Set LOA 1 for member
		ExtSource es = perun.getExtSourcesManagerBl().getExtSourceByName(sess, "INTERNAL");
		ues = new UserExtSource(es, "abc");
		ues.setLoa(1);

		User user = usersManagerEntry.getUserByMember(sess, createdMember);
		usersManagerEntry.addUserExtSource(sess, user, ues);

		// Try to extend membership
		membersManagerEntry.extendMembership(sess, createdMember);

		Attribute membershipAttributeFirst = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttributeFirst);
		assertNotNull("membership attribute value must be set", membershipAttributeFirst.getValue());

		// Try to extend membership
		assertFalse(membersManagerEntry.canExtendMembership(sess, createdMember));
	}

	@Test
	// It checks if user with insufficient LoA won't be allowed in to the VO
	public void canBeMemberLoaNotAllowed() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".canBeMemberLoaNotAllowed()");

		String loa = "1";

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<String, String>();
		extendMembershipRules.put(MembersManager.membershipDoNotAllowLoaKeyName, loa);

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		User user = usersManagerEntry.getUserByMember(sess, createdMember);

		// Try to extend membership
		assertFalse(membersManagerEntry.canBeMember(sess, createdVo, user, loa));
	}

	@Test
	// It checks if user with sufficient LoA will be allowed in to the VO
	public void canBeMemberLoaAllowed() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".canBeMemberLoaAllowed()");

		String loa = "1";

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<String, String>();
		extendMembershipRules.put(MembersManager.membershipDoNotAllowLoaKeyName, loa);

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		User user = usersManagerEntry.getUserByMember(sess, createdMember);

		// Try to extend membership
		String allowedLoa = "2";
		assertTrue(membersManagerEntry.canBeMember(sess, createdVo, user, allowedLoa));
	}

	@Test
	public void findMembersInGroup() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".findMembersInGroup()");
		Member member = setUpMember(createdVo);
		Member member2 = setUpMember2(createdVo);
		groupsManagerEntry.addMember(sess, createdGroup, member);
		groupsManagerEntry.addMember(sess, createdGroup, member2);

		List<Member> foundMembers = membersManagerEntry.findMembersInGroup(sess, createdGroup, "FirstTest");
		assertTrue(foundMembers.contains(member));
		foundMembers.remove(member);
		assertTrue(foundMembers.isEmpty());

		foundMembers = membersManagerEntry.findMembersInGroup(sess, createdGroup, "123@456.cz");
		assertTrue(foundMembers.contains(member2));
		foundMembers.remove(member2);
		assertTrue(foundMembers.isEmpty());

		foundMembers = membersManagerEntry.findMembersInGroup(sess, createdGroup, "stsdg");
		assertTrue(foundMembers.isEmpty());

		foundMembers = membersManagerEntry.findMembersInGroup(sess, createdGroup, "TestovaciLogin");
		assertTrue(foundMembers.contains(member));
		foundMembers.remove(member);
		assertTrue(foundMembers.isEmpty());
	}

	@Ignore
	@Test
	public void findMembersByName() throws Exception {
		System.out.println(MEMBERS_MANAGER_ENTRY + ".findMembersByName()");

		List<Member> members = membersManagerEntry.findMembersByName(sess, "Pepa Z Depa");

		assertTrue("results must contain at least one member",members.size() >= 1);
		assertTrue("results must contain member \"Pepa z Depa\"", members.contains(createdMember));
	}


	private Member setUpMember(Vo vo) throws Exception {

		Candidate candidate = setUpCandidate();
		Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate); // candidates.get(0)
		// set first candidate as member of test VO
		assertNotNull("No member created", member);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
		Attribute attrEmail = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_ATTR_DEF+":mail"));
		attrEmail.setValue("test@test.test");
		attributesManagerEntry.setAttribute(sess, member, attrEmail);

		User user = usersManagerEntry.getUserByMember(sess, member);
		Attribute attrLogin = new Attribute();
		attrLogin.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrLogin.setFriendlyName("login-namespace:testMichal");
		attrLogin.setType(String.class.getName());
		attrLogin = new Attribute(attributesManagerEntry.createAttribute(sess, attrLogin));
		attrLogin.setValue("TestovaciLogin");
		attributesManagerEntry.setAttribute(sess, user, attrLogin);
		return member;

	}

	private Member setUpMember2(Vo vo) throws Exception {

		Candidate candidate = setUpCandidate2();
		Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate); // candidates.get(0)
		// set first candidate as member of test VO
		assertNotNull("No member created", member);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
		Attribute attrEmail = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_ATTR_DEF+":mail"));
		attrEmail.setValue("123@456.cz");
		attributesManagerEntry.setAttribute(sess, member, attrEmail);

		User user = usersManagerEntry.getUserByMember(sess, member);
		Attribute attrLogin = new Attribute();
		attrLogin.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrLogin.setFriendlyName("login-namespace:testMichal2");
		attrLogin.setType(String.class.getName());
		attrLogin = new Attribute(attributesManagerEntry.createAttribute(sess, attrLogin));
		attrLogin.setValue("123456");
		attributesManagerEntry.setAttribute(sess, user, attrLogin);
		return member;

	}

	private Candidate setUpCandidate() {

		String userFirstName = "FirstTest";
		String userLastName = "LastTest";
		String extLogin = "ExtLoginTest";

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<String,String>());
		return candidate;

	}

	private Candidate setUpCandidate2() {

		String userFirstName = "Abcd";
		String userLastName = "Efgh";
		String extLogin = "Ijkl";

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<String,String>());
		return candidate;

	}

}
