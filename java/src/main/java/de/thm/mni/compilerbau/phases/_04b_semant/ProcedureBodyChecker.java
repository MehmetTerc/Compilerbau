package de.thm.mni.compilerbau.phases._04b_semant;

import de.thm.mni.compilerbau.absyn.*;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.absyn.visitor.Visitor;
import de.thm.mni.compilerbau.table.ProcedureEntry;
import de.thm.mni.compilerbau.table.SymbolTable;
import de.thm.mni.compilerbau.table.VariableEntry;
import de.thm.mni.compilerbau.types.ArrayType;
import de.thm.mni.compilerbau.types.PrimitiveType;
import de.thm.mni.compilerbau.types.Type;
import de.thm.mni.compilerbau.utils.NotImplemented;
import de.thm.mni.compilerbau.utils.SplError;


/**
 * This class is used to check if the currently compiled SPL program is semantically valid.
 * The body of each procedure has to be checked, consisting of {@link Statement}s, {@link Variable}s and {@link Expression}s.
 * Each node has to be checked for type issues or other semantic issues.
 * Calculated {@link Type}s can be stored in and read from the dataType field of the {@link Expression} and {@link Variable} classes.
 */
public class ProcedureBodyChecker {

    public void checkProcedures(Program program, SymbolTable globalTable) {
        //TODO (assignment 4b): Check all procedure bodies for semantic errors


        throw new NotImplemented();
    }

    public void visit(WhileStatement whileStatement) {
        whileStatement.condition.accept((Visitor) this);
        if (whileStatement.condition.dataType != PrimitiveType.boolType) {
            throw SplError.WhileConditionMustBeBoolean(whileStatement.position);
        }
    }

    public void visit(IfStatement ifStatement) {
        ifStatement.condition.accept((Visitor) this);
        if (ifStatement.condition.dataType != PrimitiveType.boolType) {
            throw SplError.WhileConditionMustBeBoolean(ifStatement.position);
        }
    }

    public void visit(ArrayTypeExpression arrayTypeExpression) {
        arrayTypeExpression.baseType.accept((Visitor) this);
        if (arrayTypeExpression.dataType != ArrayType.) {
            throw SplError.UndefinedType(arrayTypeExpression.position, arrayTypeExpression.)
        }
    }

    // SPL Error AssignmentRequiresIntegers
    public void visit(AssignStatement assignStatement) {
        assignStatement.accept((Visitor) this);
        if (assignStatement.value !=) {

        }
    }

    //  UndefinedProcedure
    public void visit(ProcedureDeclaration procedureDeclaration) {
        procedureDeclaration.accept((Visitor) this);
        procedureDeclaration.name.
    }
}





}
