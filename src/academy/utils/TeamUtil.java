package academy.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.enterprise.RevisionControlled;
import wt.fc.ObjectVector;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.org.OrganizationServicesHelper;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.project.Role;
import wt.project._Role;
import wt.session.SessionHelper;
import wt.team.Team;
import wt.team.TeamHelper;
import wt.team.TeamManaged;
import wt.team.WTRoleHolder2;
import wt.util.WTException;
import wt.vc.Iterated;

/**
 * The Team Util
 */
public class TeamUtil {

    public static final String VERSION = "$Id$";

    private static final Logger LOG = Logger.getLogger(TeamUtil.class);

    private static final String ALL_STRING = "*";

    private static final String ROLE_GROUPS = "roleGroups";

    private static final String ROLE = "Role:";

    private static final String NO_CONTAINER_TEAM = "ConainerTeam for object {0} does not exist";

    private static final String NO_CONTAINER_TEAM_PARAM = "{0}";
    

    /**
     * Checks if current user is in one of specified valid roles for a
     * containerTeamManaged object.
     * 
     * @param containerTeamManaged
     *            the containerTeamManaged (container)
     * @param validRoles
     *            the valid roles
     * @return true, if current user is in one of valid roles for
     *         containerTeamManaged
     * @throws WTException
     */
    public static boolean isUserInOneOfRoles(ContainerTeamManaged containerTeamManaged, String[] validRoles) throws WTException {
        WTPrincipal user = SessionHelper.getPrincipal();
        return isUserInOneOfRoles(user, containerTeamManaged, validRoles);
    }

    /**
     * 
     * Checks if current user is in one of specified valid roles for a
     * wtContained object.
     * 
     * @param wtContained
     * @param validRoles
     * @return
     * @throws WTException
     */
    public static boolean isUserInOneOfRoles(WTContained wtContained, String... validRoles) throws WTException {
       WTContainer container = wtContained.getContainer();
        if (container instanceof ContainerTeamManaged) {
           
            return isUserInOneOfRoles((ContainerTeamManaged) container, validRoles);
        }
        return false;
    }

    /**
     * Deprecated - use ext.eurocopter.x4.util.TeamUtil#isUserInOneOfRolesOrAdmin(wt.inf.container.WTContainer, java.lang.String[])
     *
     * Checks if current user is in one of specified valid roles for a
     * containerTeamManaged object or user is administrator.
     * 
     * @param containerTeamManaged
     *            the containerTeamManaged (container)
     * @param validRoles
     *            the valid roles
     * @return true, if current user is in one of valid roles for
     *         containerTeamManaged
     * @throws WTException
     */
    @Deprecated
    public static boolean isUserInOneOfRolesOrAdmin(ContainerTeamManaged containerTeamManaged, String[] validRoles) throws WTException {
        WTPrincipal user = SessionHelper.getPrincipal();
        return isUserInOneOfRolesOrAdmin(user, containerTeamManaged, validRoles);
    }

    public static boolean isUserInOneOfRolesOrAdmin(WTContainer container, String[] validRoles) throws WTException {
        return (isContainerTeamManaged(container) && isUserInOneOfRolesOrAdmin((ContainerTeamManaged) container, validRoles))
                || isCurrentPrincipalAdmin(container);
    }

    /**
     * 
     * Checks if current user is in one of specified valid roles for a
     * wtContained object or user is administrator.
     * 
     * @param wtContained
     * @param allowedRoles
     * @return
     * @throws WTException
     */
    public static boolean isUserInOneOfRolesOrAdmin(WTContained wtContained, String... allowedRoles)
            throws WTException {
        
        WTContainer container = wtContained.getContainer();
        
        if (container instanceof ContainerTeamManaged) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isUserInOneOfRolesOrAdmin() return: "
                        + isUserInOneOfRolesOrAdmin((ContainerTeamManaged) container, allowedRoles));
            }
            return isUserInOneOfRolesOrAdmin((ContainerTeamManaged) container, allowedRoles);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("isUserInOneOfRolesOrAdmin() return: false");
        }
        return false;
    }

    /**
     * Checks if current user is in one of specified valid roles for a Container
     * Team object.
     * 
     * @param containerTeamManaged
     *            the containerTeamManaged (container)
     * @param validRoles
     *            the valid roles
     * @return true, if current user is in one of valid roles for
     *         containerTeamManaged
     * @throws WTException
     */
    public static boolean isUserInOneOfRoles(ContainerTeam containerTeam, String[] validRoles) throws WTException {
        WTPrincipal user = SessionHelper.getPrincipal();
        return isUserInOneOfRoles(user, containerTeam, validRoles);
    }

    /**
     * Checks if current user is in one of specified valid roles for a Container
     * Team object or user is administrator.
     * 
     * @param containerTeamManaged
     *            the containerTeamManaged (container)
     * @param validRoles
     *            the valid roles
     * @return true, if current user is in one of valid roles for
     *         containerTeamManaged
     * @throws WTException
     */
    public static boolean isUserInOneOfRolesOrAdmin(ContainerTeam containerTeam, String[] validRoles) throws WTException {
        WTPrincipal user = SessionHelper.getPrincipal();
        return isUserInOneOfRolesOrAdmin(user, containerTeam, validRoles);
    }

    /**
     * Checks if user is in one of specified valid roles for a
     * containerTeamManaged object.
     * 
     * @param user
     *            the User
     * @param containerTeamManaged
     *            the containerTeamManaged (container)
     * @param validRoles
     *            the valid roles
     * @return true, if user is in one of valid roles for containerTeamManaged
     * @throws WTException
     */
    public static boolean isUserInRoles(WTPrincipal user, ContainerTeamManaged containerTeamManaged, String... validRoles) throws WTException {
    	 boolean result = false;

         if (user != null && containerTeamManaged != null && validRoles != null && validRoles.length > 0) {
             for (String role : validRoles) {
                 if (TeamUtil.isUserInRole(user, containerTeamManaged, Role.toRole(role)) || ALL_STRING.equals(role)) {
                     result = true;
                     break;
                 }
             }
         }
         return result;
    }

    /**
     * Deprecated - use ext.eurocopter.x4.util.TeamUtil#isUserInOneOfRoles(wt.org.WTPrincipal, wt.inf.container.WTContainer, java.lang.String[])
     *
     * Checks if user is in one of specified valid roles for a
     * containerTeamManaged object.
     * 
     * @param user
     *            the User
     * @param containerTeamManaged
     *            the containerTeamManaged (container)
     * @param validRoles
     *            the valid roles
     * @return true, if user is in one of valid roles for containerTeamManaged
     * @throws WTException
     */
    @Deprecated
    public static boolean isUserInOneOfRoles(WTPrincipal user, ContainerTeamManaged containerTeamManaged, String[] validRoles) throws WTException {
        boolean result = false;

        if (user != null && containerTeamManaged != null && validRoles != null && validRoles.length > 0) {
            for (String role : validRoles) {
                if (TeamUtil.isUserInRole(user, containerTeamManaged, Role.toRole(role)) || ALL_STRING.equals(role)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    public static boolean isUserInOneOfRoles(WTPrincipal user, WTContainer container, String[] validRoles) throws WTException {
        return (isContainerTeamManaged(container)) && isUserInOneOfRoles(user, (ContainerTeamManaged) container, validRoles);
    }

    /**
     * Deprecated - use ext.eurocopter.x4.util.TeamUtil#isUserInOneOfRolesOrAdmin(wt.org.WTPrincipal, wt.inf.container.WTContainer, java.lang.String[])
     *
     * Checks if user is in one of specified valid roles for a
     * containerTeamManaged object or user is administrator.
     * 
     * @param user
     *            the User
     * @param containerTeamManaged
     *            the containerTeamManaged (container)
     * @param validRoles
     *            the valid roles
     * @return true, if user is in one of valid roles for containerTeamManaged
     * @throws WTException
     */
    @Deprecated
    public static boolean isUserInOneOfRolesOrAdmin(WTPrincipal user, ContainerTeamManaged containerTeamManaged, String[] validRoles) throws WTException {
        boolean result = false;

        if (user != null && containerTeamManaged != null && validRoles != null && validRoles.length > 0) {
            for (String role : validRoles) {
                if (TeamUtil.isUserInRole(containerTeamManaged, Role.toRole(role)) || ALL_STRING.equals(role)) {
                    result = true;
                    break;
                }
            }
        }

        if (!result) {
            result = isAdministrator(containerTeamManaged, user);
        }

        return result;
    }

    public static boolean isUserInOneOfRolesOrAdmin(WTPrincipal user, WTContainer container, String[] validRoles) throws WTException {
        return (isContainerTeamManaged(container) && isUserInOneOfRolesOrAdmin(user, (ContainerTeamManaged) container, validRoles))
                || isPrincipalAdmin(container, user);
    }

    /**
     * Checks if user is in one of specified valid roles for a
     * containerTeamManaged object or the creator of the object or user is
     * administrator.
     * 
     * @param user
     *            the User
     * @param containerTeamManaged
     *            the containerTeamManaged (container)
     * @param persistable
     *            The object whose creator is checked.
     * @param validRoles
     *            the valid roles
     * @return true, if user is in one of valid roles for containerTeamManaged
     * @throws WTException
     */
    public static boolean isUserInOneOfRolesOrCreatorOrAdmin(WTPrincipal user, ContainerTeamManaged containerTeamManaged, RevisionControlled persistable, String[] validRoles) throws WTException {
        boolean result = isUserInOneOfRolesOrAdmin(user, containerTeamManaged, validRoles);

        if (!result) {
            result = persistable.getCreator().getPrincipal().equals(user);
        }

        return result;
    }
	
	/**
     * Checks if user is in one of specified valid roles for a
     * containerTeamManaged object or the creator of the object or user is
     * administrator.
     * 
     * @param user
     *            the User
     * @param containerTeamManaged
     *            the containerTeamManaged (container)
     * @param persistable
     *            The object whose creator is checked.
     * @param validRoles
     *            the valid roles
     * @return true, if user is in one of valid roles for containerTeamManaged
     * @throws WTException
     */
    public static boolean isUserInOneOfRolesOrCreatorOrAdmin(WTPrincipal user, ContainerTeamManaged containerTeamManaged, Iterated persistable, String[] validRoles) throws WTException {
        boolean result = isUserInOneOfRolesOrAdmin(user, containerTeamManaged, validRoles);

        if (!result) {
            result = persistable.getIterationInfo().getCreatorOrig().getPrincipal().equals(user);
        }

        return result;
    }

    /**
     * Checks if the current user is a guest.
     * 
     * <p>
     * <b>Note:</b> for some reason, the isUserInRole methods, e.g.,
     * {@link #isUserInOneOfRoles(WTContained, String...)}, do not work for
     * guests. If someone has any spare, this may be interesting to generalize
     * this or the "old" methods.
     * </p>
     * 
     * @param contained
     *            The contained object to check.
     * 
     * @return {@code true} if the user is in the {@link Role} GUEST,
     *         {@code false} otherwise.
     * 
     * @see SessionHelper#getPrincipal()
     */
    public static boolean isGuest(WTContained contained) {

        boolean retval = false;
        WTPrincipal currentUser = null;
        try {
            currentUser = SessionHelper.getPrincipal();

            ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam((ContainerTeamManaged) contained.getContainer());
            Map<?, ?> rolePrincipalMap = containerTeam.getRolePrincipalMap();
            List<?> roleList = (List<?>) rolePrincipalMap.get("GUEST");

            WTGroup guestGroup = (WTGroup) ((WTPrincipalReference) roleList.get(0)).getObject();

            retval = guestGroup.isMember(SessionHelper.getPrincipal());
        } catch (ClassCastException | WTException e) {
            LOG.error("Exception occured while testing the user '" + currentUser + "' for being guest.", e);
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("isGuest - returning - " + retval);
        }

        return retval;
    }

    /**
     * Checks if user is in one of specified valid roles for a Container Team
     * object.
     * 
     * @param user
     *            the User
     * @param containerTeamManaged
     *            the containerTeamManaged (container)
     * @param validRoles
     *            the valid roles
     * @return true, if user is in one of valid roles for containerTeamManaged
     * @throws WTException
     */
    public static boolean isUserInOneOfRoles(WTPrincipal user, ContainerTeam containerTeam, String[] validRoles) throws WTException {
        boolean result = false;

        if (user != null && containerTeam != null && validRoles != null && validRoles.length > 0) {
            for (String role : validRoles) {
                if (TeamUtil.isUserInRole(containerTeam, Role.toRole(role)) || ALL_STRING.equals(role)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Checks if user is in one of specified valid roles for a Container Team
     * object or user is administrator.
     * 
     * @param user
     *            the User
     * @param containerTeamManaged
     *            the containerTeamManaged (container)
     * @param validRoles
     *            the valid roles
     * @return true, if user is in one of valid roles for containerTeamManaged
     * @throws WTException
     */
    public static boolean isUserInOneOfRolesOrAdmin(WTPrincipal user, ContainerTeam containerTeam, String[] validRoles) throws WTException {
        boolean result = false;

        if (user != null && containerTeam != null && validRoles != null && validRoles.length > 0) {
            for (String role : validRoles) {
                if (TeamUtil.isUserInRole(containerTeam, Role.toRole(role)) || ALL_STRING.equals(role)) {
                    result = true;
                    break;
                }
            }
        }

        if(!result) {
            result = isAdministrator(containerTeam, user);
        }

        return result;
    }

    /**
     * Deprecated - use ext.eurocopter.x4.util.TeamUtil#isUserInRole(wt.inf.container.WTContainer, wt.project.Role...)
     *
     * Checks if logged user is in current role for current container.
     * 
     * @param containerTeamManaged
     *            the containerTeamManaged (container)
     * @param role
     *            the role
     * 
     * @return true, if checks if is user in role
     * 
     * @throws WTException
     *             the WT exception
     */
    @Deprecated
    public static boolean isUserInRole(ContainerTeamManaged containerTeamManaged, Role... roles) throws WTException {
        WTPrincipal user = SessionHelper.getPrincipal();
        for (Role role : roles) {
			if (isUserInRole(user, containerTeamManaged, role)) {
				return true;
			}
		}
        return false;
    }

    public static boolean isUserInRole(WTContainer container, Role... roles) throws WTException {
        return (isContainerTeamManaged(container)) && isUserInRole((ContainerTeamManaged) container, roles);
    }

    /**
     * Deprecated - use ext.eurocopter.x4.util.TeamUtil#isUserInRole(wt.org.WTPrincipal, wt.inf.container.WTContainer, wt.project.Role)
     *
     * Checks if passed user is in current role for current container.
     * 
     * @param user
     *            the user
     * @param containerTeamManaged
     *            the containerTeamManaged (container)
     * @param role
     *            the role
     * 
     * @return true, if checks if is user in role
     * 
     * @throws WTException
     *             the WT exception
     */
    @Deprecated
    public static boolean isUserInRole(WTPrincipal user, ContainerTeamManaged containerTeamManaged, Role role) throws WTException {
        ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam(containerTeamManaged);
        return isUserInRole(user, containerTeam, role);
    }

    public static boolean isUserInRole(WTPrincipal user, WTContainer container, Role role) throws WTException {
        return (isContainerTeamManaged(container)) && isUserInRole(user, (ContainerTeamManaged) container, role);
    }

    /**
     * Checks if passed user is in current role for Container Team
     * 
     * @param user
     *            the user
     * @param containerTeamManaged
     *            the containerTeamManaged (container)
     * @param role
     *            the role
     * 
     * @return true, if checks if is user in role
     * 
     * @throws WTException
     *             the WT exception
     */
    public static boolean isUserInRole(WTPrincipal user, ContainerTeam containerTeam, Role role) throws WTException {
        if (containerTeam == null || user == null || role == null) {
            return false;
        }

        ObjectVector objectvector = new ObjectVector();
        Map<?, ?> rolePrincipalMap = containerTeam.getRolePrincipalMap();
        findContainerTeamPrincipals(rolePrincipalMap, "roleGroups", objectvector, true);
        Enumeration<?> roleGroups = objectvector.elements();
        while (roleGroups.hasMoreElements()) {
            WTGroup group = (WTGroup) roleGroups.nextElement();
            Role groupRole = Role.toRole(group.getName());
            if (StringUtils.equalsIgnoreCase(role.toString(), groupRole.toString())) {
                return group.isMember(user);
            }
        }
        return false;
    }

    /**
     * Checks if current user is in current role for Container Team
     * 
     * @param user
     *            the user
     * @param containerTeamManaged
     *            the containerTeamManaged (container)
     * @param role
     *            the role
     * 
     * @return true, if checks if is user in role
     * 
     * @throws WTException
     *             the WT exception
     */
    public static boolean isUserInRole(ContainerTeam containerTeam, Role role) throws WTException {
        WTPrincipal user = SessionHelper.getPrincipal();
        return isUserInRole(user, containerTeam, role);
    }

    /**
     * This method will find the principals in a map at the level relating to
     * the parameter groupName.
     * 
     * @param rolePrincipalMap
     *            The role principal map for the container.
     * @param groupName
     *            The name of the group that will be set as the scope.
     * @param principals
     *            A vector of principals that this method will add to.
     * @return principals An enumeration of the principals that were found and
     *         appended to the parameter.
     * @param returnUsers
     *            A boolean stating whether to return the users or the group.
     * @exception WTException
     */
    public static void findContainerTeamPrincipals(Map<?, ?> rolePrincipalMap, String groupName, ObjectVector principals, boolean returnUsers)
            throws WTException {
        Role role = Role.toRole(groupName);
        List<?> list = (List<?>) rolePrincipalMap.get(role);
        if (list != null && list.size() > 0) {
            WTPrincipalReference wtprincipalreference = (WTPrincipalReference) list.get(0);
            if (returnUsers) {
                WTPrincipal wtprincipal = wtprincipalreference.getPrincipal();
                Enumeration<?> enumeration = OrganizationServicesHelper.manager.members((WTGroup) wtprincipal, false);
                while (enumeration.hasMoreElements()) {
                    principals.addElement(enumeration.nextElement());
                }
            } else {
                principals.addElement(wtprincipalreference.getPrincipal());
            }
        }
    }

    /**
     * Sets role for user.
     * 
     * @param user
     *            the user
     * @param container
     *            the container
     * @param role
     *            the role
     * 
     * @return true, if sets the role for user
     * 
     * @throws WTException
     *             the WT exception
     */
    public static boolean setRoleForUser(WTPrincipal user, ContainerTeamManaged container, Role role) throws WTException {
        ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam(container);
        if (containerTeam == null) {
            LOG.error("ConainerTeam for object [" + container + "] does not exist");
            return false;
        }
        QueryResult queryresult = ContainerTeamHelper.service.findRolePrincipalMap(role, user, containerTeam);
        if (!queryresult.hasMoreElements()) {
            containerTeam.addPrincipal(role, user);
            return true;
        }
        return false;
    }

    /**
     * Sets role for logged user.
     * 
     * @param container
     *            the container
     * @param role
     *            the role
     * 
     * @return true, if sets the role for user
     * 
     * @throws WTException
     *             the WT exception
     */
    public static boolean setRoleForUser(ContainerTeamManaged container, Role role) throws WTException {
        WTPrincipal user = SessionHelper.getPrincipal();
        return setRoleForUser(user, container, role);
    }

    /**
     * Whether or not the current user is an Administrator for the container.
     */
    public static boolean isAdministrator(ContainerTeamManaged container) throws WTException {
        return isAdministrator(container, SessionHelper.getPrincipal());
    }

    /**
     * Whether or not the current user is an Administrator for the container.
     */
    public static boolean isAdministrator(ContainerTeamManaged container, WTPrincipal user) throws WTException {
        return WTContainerHelper.service.isAdministrator(container.getContainerReference(), user);
    }

    /**
     * Whether or not the current user is an Administrator for the container.
     */
    public static boolean isAdministrator(ContainerTeam containerTeam) throws WTException {
        return isAdministrator(containerTeam, SessionHelper.getPrincipal());
    }

    /**
     * Whether or not the current user is an Administrator for the container.
     */
    public static boolean isAdministrator(ContainerTeam containerTeam, WTPrincipal user) throws WTException {
        return WTContainerHelper.service.isAdministrator(containerTeam.getContainerReference(), user);
    }

    /**
     * Whether or not the current user is site admin
     */
    public static boolean isSiteAdministrator(WTPrincipal user) throws WTException {
        boolean isSiteAdmin =false;
        WTContainerRef siteContainerRef = WTContainerHelper.service.getExchangeRef();
        if (siteContainerRef != null) {
            return WTContainerHelper.service.isAdministrator(siteContainerRef, user);
        }
        return isSiteAdmin;
    }


    /**
     * Checks if the given user is a member of the group
     * 
     * @param user
     *            user to check
     * @param groupName
     *            group name
     * @return true if user is member of this group, otherwise false
     * @throws WTException
     */
    public static boolean isMemberOfGroup(WTUser user, String groupName) throws WTException {
        return isMemberOfGroup(user, new String[] { groupName });
    }

    /**
     * Checks if the given user is a member of one of the groups
     * 
     * @param user
     *            user to check
     * @param groupName
     *            group name
     * @return true if user is member of this group, otherwise false
     * @throws WTException
     */
    @SuppressWarnings("rawtypes")
    public static boolean isMemberOfGroup(WTUser user, String[] groupNamesArr) throws WTException {
        boolean result = false;
        if (user != null && groupNamesArr != null) {
            List<String> groupNames = Arrays.<String> asList(groupNamesArr);
            WTSet parentGroups = new WTHashSet();
            getParentGroups(user, parentGroups);

            Iterator parentGroupsIterator = parentGroups.persistableIterator(WTGroup.class, true);
            while (parentGroupsIterator.hasNext()) {
                WTGroup parentGroup = (WTGroup) parentGroupsIterator.next();
                if (groupNames.contains(parentGroup.getName())) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Method recurrently search for parent groups of WTPrincipal
     * 
     * @param principal
     *            WTPrincipal to search in
     * @param parentGroups
     *            WTSet of parent groups returned by this method
     * @throws WTException
     */
    @SuppressWarnings("rawtypes")
    public static void getParentGroups(WTPrincipal principal, WTSet parentGroups) throws WTException {
        Enumeration groupsPrincipalIsMemberOf = null;
        if (principal instanceof WTUser) {
            groupsPrincipalIsMemberOf = ((WTUser) principal).parentGroupObjects();
        } else if (principal instanceof WTGroup) {
            groupsPrincipalIsMemberOf = ((WTGroup) principal).parentGroups();
        }

        while (groupsPrincipalIsMemberOf.hasMoreElements()) {
            Object obj = groupsPrincipalIsMemberOf.nextElement();

            if (obj instanceof WTGroup) {
                WTGroup group = (WTGroup) obj;
                parentGroups.add(group);
                getParentGroups(group, parentGroups);
            } else if (obj instanceof WTPrincipalReference) {
                WTPrincipalReference groupRef = (WTPrincipalReference) obj;
                WTGroup group = (WTGroup) groupRef.getObject();
                parentGroups.add(group);
                getParentGroups(group, parentGroups);
            }
        }
    }

    public static WTGroup getRoleGroup(ContainerTeam containerTeam, Role role) throws WTException {
        if (containerTeam == null || role == null) {
            return null;
        }

        ObjectVector objectvector = new ObjectVector();
        Map<?, ?> rolePrincipalMap = containerTeam.getRolePrincipalMap();
        findContainerTeamPrincipals(rolePrincipalMap, "roleGroups", objectvector, true);
        Enumeration<?> roleGroups = objectvector.elements();
        while (roleGroups.hasMoreElements()) {
            WTGroup group = (WTGroup) roleGroups.nextElement();
            Role groupRole = Role.toRole(group.getName());
            if (StringUtils.equalsIgnoreCase(role.toString(), groupRole.toString())) {
                return group;
            }
        }

        return null;
    }

    public static WTGroup getRoleGroup(ContainerTeamManaged containerTeamManaged, Role role) throws WTException {
        if (containerTeamManaged == null || role == null) {
            return null;
        }
        ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam(containerTeamManaged);
        return getRoleGroup(containerTeam, role);
    }

    public static HashSet<String> getContainerTeamManagedRoles(ContainerTeamManaged containerTeamManaged) throws WTException {
        if (containerTeamManaged == null) {
            return new HashSet<String>();
        }

        ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam(containerTeamManaged);
        return getContainerTeamRoles(containerTeam);
    }

    public static HashSet<String> getContainerTeamRoles(ContainerTeam containerTeam) throws WTException {
        HashSet<String> roles = new HashSet<String>();

        if (containerTeam != null) {
            ObjectVector objectvector = new ObjectVector();
            Map<?, ?> rolePrincipalMap = containerTeam.getRolePrincipalMap();
            findContainerTeamPrincipals(rolePrincipalMap, "roleGroups", objectvector, true);
            Enumeration<?> roleGroups = objectvector.elements();
            while (roleGroups.hasMoreElements()) {
                WTGroup group = (WTGroup) roleGroups.nextElement();
                roles.add(group.getName());
            }
        }

        return roles;
    }

    
    /**
     * Method gets users from container with specific role.
     * Groups are automatically resolved.
     * 
     * @param team
     * @param role
     * 
     * @return List<WTPrincipal>
     * 
     * @throws WTException
     */
    public static List<WTUser> getUsers(WTRoleHolder2 source, Role role)
            throws WTException {
        List<WTUser> result = new ArrayList<WTUser>();

        Map<?, ?> principalMap = source.getRolePrincipalMap();
        List<?> principals = (List<?>) principalMap.get(role);
        for (Object principal : principals) {
            if (principal instanceof WTPrincipalReference) {
                principal = ((WTPrincipalReference) principal).getPrincipal();
            }

            if (principal instanceof WTUser) {
                result.add((WTUser) principal);
            } else if (principal instanceof WTGroup) {
                WTGroup group = (WTGroup) principal;
                Enumeration<?> members = group.members();
                while (members.hasMoreElements()) {
                    WTPrincipal user = (WTPrincipal) members.nextElement();
                    if (user instanceof WTUser) {
                        result.add((WTUser) user);
                    }
                }
            }
        }
        return result;
    }


    /**
     * Return all user roles from container.
     * 
     * @param user
     *            the user
     * @param container
     *            the container
     * 
     * @return List<Role>
     * 
     * @throws WTException
     *             the WT exception
     */
    public static List<Role> getUserRoles(WTPrincipal user, ContainerTeamManaged container) throws WTException {
        List<Role> result = new ArrayList<Role>();
        ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam(container);
        if (containerTeam == null) {
            LOG.error(NO_CONTAINER_TEAM.replace(NO_CONTAINER_TEAM_PARAM, container.toString()));
            return result;
        }
        
        return getUserRoles(user, containerTeam);
    }
    
    public static List<Role> getUserRoles(WTPrincipal user, WTRoleHolder2 roleHolder) throws WTException {
        List<Role> result = new ArrayList<Role>();
    
        ObjectVector objectvector = new ObjectVector();
        Map<?, ?> map = roleHolder.getRolePrincipalMap();
        findContainerTeamPrincipals(map, ROLE_GROUPS, objectvector, true);
        Enumeration<?> roleGroups = objectvector.elements();
        LOG.debug("User:" + user.getName());
        while (roleGroups.hasMoreElements()) {
            WTGroup group = (WTGroup) roleGroups.nextElement();
            LOG.debug(ROLE + group.getName());
            if (group.isMember(user)) {
                result.add(_Role.toRole(group.getName()));
            }
        }
        return result;
    }
    
	public static boolean isUserInAnotherRoleThanReadOnly(WTPrincipal user, Persistable teamManagedPersistable) throws WTException {
		if (teamManagedPersistable instanceof TeamManaged) {
			Team objTeam = TeamHelper.service.getTeam((TeamManaged) teamManagedPersistable);
			return isUserInAnotherRoleThanReadOnly(user, objTeam);
		} else if(teamManagedPersistable instanceof ContainerTeamManaged){
			 ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam((ContainerTeamManaged) teamManagedPersistable);
			 return isUserInAnotherRoleThanReadOnly(user, containerTeam);
		}
		return false;
	}

    private static boolean isContainerTeamManaged(WTContainer container) {
        return container != null && ContainerTeamManaged.class.isInstance(container);
    }

    private static boolean isPrincipalAdmin(WTContainer container, WTPrincipal principal) throws WTException {
        if (container == null || principal == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Container or principal is null - returning true");
            }
            return false;
        }
        return WTContainerHelper.service.isAdministrator(WTContainerRef.newWTContainerRef(container), principal);
    }

    private static boolean isCurrentPrincipalAdmin(WTContainer container) throws WTException {
        return isPrincipalAdmin(container, SessionHelper.getPrincipal());
    }

}
