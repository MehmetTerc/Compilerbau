package de.thm.mni.compilerbau.phases._04b_semant;

import de.thm.mni.compilerbau.absyn.*;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.absyn.visitor.Visitor;
import de.thm.mni.compilerbau.phases._04a_tablebuild.TableBuilder;
import de.thm.mni.compilerbau.table.Entry;
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
    public SymbolTable globalTable;

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

    // value mit target vergleichen
    // SPL Error AssignmentRequiresIntegers
    public void visit(AssignStatement assignStatement) {
        assignStatement.accept((Visitor) this);
        if (assignStatement.target.dataType != PrimitiveType.intType) {
            throw SplError.AssignmentRequiresIntegers(assignStatement.position);
        }
        if (assignStatement.target.dataType != assignStatement.value.dataType) {
            throw SplError.AssignmentHasDifferentTypes(assignStatement.position);
        }
    }

    public void visit(ArrayAccess arrayAccess) {
        arrayAccess.accept((Visitor) this);

        if (!(arrayAccess.array.dataType instanceof ArrayType)) {
            throw SplError.IndexingNonArray(arrayAccess.position);
        }
        if (arrayAccess.index.dataType != PrimitiveType.intType) {
            throw SplError.IndexingWithNonInteger(arrayAccess.position);
        }
    }

    public void visit(CallStatement callStatement) {
        callStatement.arguments.forEach(n -> n.accept((Visitor) this));
        Entry entry = globalTable.lookup(callStatement.procedureName,SplError.UndefinedProcedure(callStatement.position,callStatement.procedureName));
        if(!(entry instanceof ProcedureEntry)){
            throw SplError.CallOfNonProcedure(callStatement.position,callStatement.procedureName);
        }
        ProcedureEntry procedureEntry = (ProcedureEntry) entry;
        if(procedureEntry.parameterTypes.size()>callStatement.arguments.size()){
            throw SplError.TooFewArguments(callStatement.position,callStatement.procedureName);
        }
        if(procedureEntry.parameterTypes.size()<callStatement.arguments.size()){
            throw SplError.TooManyArguments(callStatement.position,callStatement.procedureName);
        }
    }

    public void visit(BinaryExpression binaryExpression) {
        binaryExpression.accept((Visitor) this);

        if (binaryExpression.leftOperand.dataType != binaryExpression.rightOperand.dataType) {
            throw SplError.OperatorDifferentTypes(binaryExpression.position);
        }

        if (binaryExpression.operator.isComparison() && (binaryExpression.leftOperand.dataType != PrimitiveType.intType ||
                binaryExpression.rightOperand.dataType != PrimitiveType.intType)) {
            throw SplError.ComparisonNonInteger(binaryExpression.position);
        }

        if (binaryExpression.operator.isArithmetic() && (binaryExpression.leftOperand.dataType != PrimitiveType.intType ||
                binaryExpression.rightOperand.dataType != PrimitiveType.intType)) {
            throw SplError.ArithmeticOperatorNonInteger(binaryExpression.position);
        }
    }

    public void visit(NamedTypeExpression namedTypeExpression){

    }

    public void visit(NamedVariable namedVariable){
        namedVariable.accept((Visitor) this);
    }

}







