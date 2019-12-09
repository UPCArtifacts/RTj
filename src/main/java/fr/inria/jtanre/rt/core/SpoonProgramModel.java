package fr.inria.jtanre.rt.core;

import java.util.List;

import fr.inria.astor.core.manipulation.MutationSupporter;
import spoon.reflect.declaration.CtType;

/**
 * 
 * @author Matias Martinez
 *
 */
public class SpoonProgramModel extends ProgramModel<CtType<?>> {

	@Override
	public List<CtType<?>> getAllClasses() {
		return MutationSupporter.getFactory().Class().getAll();

	}

}
