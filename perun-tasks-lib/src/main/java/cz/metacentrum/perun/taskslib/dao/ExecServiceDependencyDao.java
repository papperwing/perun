package cz.metacentrum.perun.taskslib.dao;

import java.util.List;

import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;

/**
 * ExecService Dependency Data Access Object
 *
 * @author Michal Karm Babacek
 */
public interface ExecServiceDependencyDao {
	/**
	 * Create dependency
	 * The execService can not be executed if any of the execServices it depends
	 * on is in an unstable (not terminal) state.
	 *
	 * @param dependantServiceId
	 * 			The execService depending on the other execService.
	 * @param execServiceId
	 * 			The execService the other execService depends on.
	 */
	public void createDependency(int dependantServiceId, int execServiceId);
	/**
	 * Remove dependency
	 * Removes the dependency...
	 *
	 * @param dependantServiceId
	 * 			The execService depending on the other execService.
	 * @param execServiceId
	 * 			The execService the other execService depends on.
	 */
	public void removeDependency(int dependantServiceId, int execServiceId);

	/**
	 * Is there a dependency?
	 * Checks whether there the one execService depends on the other.
	 *
	 * @param dependantServiceId
	 * 			The execService depending on the other execService.
	 * @param execServiceId
	 * 			The execService the other execService depends on.
	 * @return	true - yes, there is such a dependency
	 * 			false - no, there is not such a dependency
	 */
	public boolean isThereDependency(int dependantServiceId, int execServiceId);

	/**
	 * List execServices depending on the given execService
	 *
	 * @param execServiceId
	 * 			The execService which dependent execServices we want to look up.
	 * @return	A list of execServices that are depending on the given execService.
	 */
	public List<ExecService> listExecServicesDependingOn(int execServiceId);

	/**
	 * List execServices this execService depends on
	 *
	 * @param dependantServiceId
	 * 			The execService which dependencies we want to look up.
	 * @return A list of execServices this execService depends on.
	 */
	public List<ExecService> listExecServicesThisExecServiceDependsOn(int dependantServiceId);

	/**
	 * List execServices this execService depends on
	 * @param dependantServiceId
	 * @return
	 */
	public List<ExecService> listExecServicesThisExecServiceDependsOn(int dependantExecServiceId, ExecServiceType execServiceType);
}
