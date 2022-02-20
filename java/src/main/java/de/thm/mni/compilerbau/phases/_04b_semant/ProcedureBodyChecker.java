package de.thm.mni.compilerbau.phases._04b_semant;

import de.thm.mni.compilerbau.absyn.*;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.absyn.visitor.Visitor;
import de.thm.mni.compilerbau.phases._04a_tablebuild.TableBuilder;
import de.thm.mni.compilerbau.table.*;
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
public class ProcedureBodyChecker extends DoNothingVisitor {

    public void checkProcedures(Program program, SymbolTable globalTable) {
        //TODO (assignment 4b): Check all procedure bodies for semantic errors
        ProcedureBodyCheckerVisitor procedureBodyCheckerVisitor = new ProcedureBodyCheckerVisitor(globalTable);
        program.accept(procedureBodyCheckerVisitor);

    }

    class ProcedureBodyCheckerVisitor extends DoNothingVisitor {
        private SymbolTable table;
        private SymbolTable localTable;

        public ProcedureBodyCheckerVisitor(SymbolTable symbolTable) {
            this.table = symbolTable;
        }

        public void visit(Program program){
            program.declarations.forEach(pd -> pd.accept(this));
        }

        public void visit(WhileStatement whileStatement) {
            whileStatement.condition.accept((Visitor) this);
            if (whileStatement.condition.dataType != PrimitiveType.boolType) {
                throw SplError.WhileConditionMustBeBoolean(whileStatement.position);
            }
            whileStatement.body.accept(this);
        }

        public void visit(IfStatement ifStatement) {
            ifStatement.condition.accept((Visitor) this);
            if (ifStatement.condition.dataType != PrimitiveType.boolType) {
                throw SplError.IfConditionMustBeBoolean(ifStatement.position);
            }
            ifStatement.thenPart.accept(this);
            ifStatement.elsePart.accept(this);
        }


        public void visit(AssignStatement assignStatement) {
            assignStatement.target.accept (this);
            assignStatement.value.accept(this);
            if (assignStatement.target.dataType != PrimitiveType.intType) {
                throw SplError.AssignmentRequiresIntegers(assignStatement.position);
            }
            if (assignStatement.target.dataType != assignStatement.value.dataType) {
                throw SplError.AssignmentHasDifferentTypes(assignStatement.position);
            }
        }

        public void visit(ArrayAccess arrayAccess) {
            arrayAccess.array.accept(this);
            if (!(arrayAccess.array.dataType instanceof ArrayType)) {
                throw SplError.IndexingNonArray(arrayAccess.position);
            }
            arrayAccess.index.accept(this);
            if (arrayAccess.index.dataType != PrimitiveType.intType) {
                throw SplError.IndexingWithNonInteger(arrayAccess.position);
            }
            ArrayType arrayType =  (ArrayType) arrayAccess.array.dataType;
            arrayAccess.dataType = arrayType.baseType;
        }

        public void visit(CallStatement callStatement) {
            Entry entry = table.lookup(callStatement.procedureName, SplError.UndefinedProcedure(callStatement.position, callStatement.procedureName));
            if (!(entry instanceof ProcedureEntry)) {
                throw SplError.CallOfNonProcedure(callStatement.position, callStatement.procedureName);
            }
            ProcedureEntry procedureEntry = (ProcedureEntry) entry;
            if (procedureEntry.parameterTypes.size() > callStatement.arguments.size()) {
                throw SplError.TooFewArguments(callStatement.position, callStatement.procedureName);
            }
            if (procedureEntry.parameterTypes.size() < callStatement.arguments.size()) {
                throw SplError.TooManyArguments(callStatement.position, callStatement.procedureName);
            }
            for (int i = 0; i < callStatement.arguments.size(); i++) {
                if (procedureEntry.parameterTypes.get(i).isReference && !(callStatement.arguments.get(i) instanceof VariableExpression)) {
                    throw SplError.ArgumentMustBeAVariable(callStatement.position, callStatement.procedureName, i + 1);
                }
                if (procedureEntry.parameterTypes.get(i).type != callStatement.arguments.get(i).dataType) {
                    throw SplError.ArgumentTypeMismatch(callStatement.position, callStatement.procedureName, i + 1);
                }
                callStatement.arguments.get(i).accept(this);
            }

        }

        protected void checkType(Type expected, Type actual, SplError error) throws SplError {
            if(!expected.equals(actual)){
                throw error;
            }
        }

        public void visit(ProcedureDeclaration procedureDeclaration){
            ProcedureEntry entry =(ProcedureEntry) table.lookup(procedureDeclaration.name);
            localTable = entry.localTable;
            procedureDeclaration.body.forEach(n->n.accept(this));
        }

        public void visit(BinaryExpression binaryExpression) {
            binaryExpression.leftOperand.accept(this);
            binaryExpression.rightOperand.accept(this);
            if(binaryExpression.operator.isArithmetic()){
                if (binaryExpression.leftOperand.dataType.equals(PrimitiveType.boolType) && binaryExpression.rightOperand.dataType.equals(PrimitiveType.boolType)) throw SplError.ArithmeticOperatorNonInteger(binaryExpression.position);
                checkType(PrimitiveType.intType, binaryExpression.leftOperand.dataType, SplError.OperatorDifferentTypes(binaryExpression.position));
                checkType(PrimitiveType.intType, binaryExpression.rightOperand.dataType, SplError.OperatorDifferentTypes(binaryExpression.position));
                binaryExpression.dataType = PrimitiveType.intType;
            } else {
                if (binaryExpression.leftOperand.dataType.equals(PrimitiveType.boolType) && binaryExpression.rightOperand.dataType.equals(PrimitiveType.boolType)) throw SplError.ComparisonNonInteger(binaryExpression.position);
                checkType(PrimitiveType.intType, binaryExpression.leftOperand.dataType, SplError.OperatorDifferentTypes(binaryExpression.position));
                checkType(PrimitiveType.intType, binaryExpression.rightOperand.dataType, SplError.OperatorDifferentTypes(binaryExpression.position));
                binaryExpression.dataType = PrimitiveType.boolType;
            }
        }


        public void visit(NamedVariable namedVariable) {
            Entry entry = localTable.lookup(namedVariable.name, SplError.UndefinedVariable(namedVariable.position, namedVariable.name));
            if (!(entry instanceof VariableEntry)) {
                throw SplError.NotAVariable(namedVariable.position, namedVariable.name);
            }
            namedVariable.dataType = ((VariableEntry) entry).type;
        }

        public void visit(IntLiteral intLiteral){
            intLiteral.dataType=PrimitiveType.intType;
        }

        public void visit(CompoundStatement compoundStatement){
            compoundStatement.statements.forEach(cs->cs.accept(this));
        }

        public void visit(VariableExpression variableExpression) {
            variableExpression.variable.accept(this);
            variableExpression.dataType = variableExpression.variable.dataType;
        }

    }

}







