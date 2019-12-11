package fr.inria.jtanre.rt.processors;

import fr.inria.astor.approaches.jgenprog.operators.InsertBeforeOp;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.StatementOperatorInstance;
import fr.inria.astor.core.manipulation.MutationSupporter;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtComment.CommentType;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;

/**
 * 
 * @author Matias Martinez
 *
 */
public class RepairFactory {

	public static ProgramVariant createCommentRefactor(CtClass aTestModelCtClass, CtElement rottenElement) {
		ProgramVariant pv = new ProgramVariant();
		pv.getBuiltClasses().put(aTestModelCtClass.getQualifiedName(), aTestModelCtClass);
		ModificationPoint mp = new ModificationPoint();
		mp.setCodeElement(rottenElement);
		mp.setProgramVariant(pv);
		mp.setCtClass(aTestModelCtClass);
		pv.getModificationPoints().add(mp);

		///
		CtComment comment = MutationSupporter.getFactory().createComment("TODO: Rotten element:\n", CommentType.BLOCK);

		InsertBeforeOp insertBefore = new InsertBeforeOp();
		OperatorInstance commentOpInstance = new StatementOperatorInstance(mp, insertBefore, rottenElement, comment);

		pv.getOperations(1).add(commentOpInstance);
		return pv;
	}
}
