package de.thm.mni.compilerbau.phases._06_codegen;

import de.thm.mni.compilerbau.absyn.*;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.table.ProcedureEntry;
import de.thm.mni.compilerbau.table.SymbolTable;
import de.thm.mni.compilerbau.table.VariableEntry;
import de.thm.mni.compilerbau.types.ArrayType;
import de.thm.mni.compilerbau.utils.NotImplemented;
import de.thm.mni.compilerbau.utils.SplError;

import java.io.PrintWriter;

/**
 * This class is used to generate the assembly code for the compiled program.
 * This code is emitted via the {@link CodePrinter} in the output field of this class.
 */
public class CodeGenerator {
    private final CodePrinter output;
    private final boolean ershovOptimization;

    /**
     * Initializes the code generator.
     *
     * @param output             The PrintWriter to the output file.
     * @param ershovOptimization Whether the ershov register optimization should be used (--ershov)
     */

    private class CodeGeneratorVisitor extends DoNothingVisitor {
        SymbolTable localTable;
        SymbolTable globalTable;
        int lblCounter = 0;

        public CodeGeneratorVisitor(SymbolTable symbolTable) {
            this.globalTable = symbolTable;
        }

        Register register = new Register(8);

        public void visit(IntLiteral intLiteral) {
            output.emitInstruction("add", register, new Register(0), intLiteral.value);
        }

        public void visit(BinaryExpression binaryExpression) {
            binaryExpression.leftOperand.accept(this);
            binaryExpression.rightOperand.accept(this);
            switch (binaryExpression.operator) {
                case ADD:
                    output.emitInstruction("add", register.minus(2), register.minus(2), register.minus(1));
                    break;
                case SUB:
                    output.emitInstruction("sub", register.minus(2), register.minus(2), register.minus(1));
                    break;
                case MUL:
                    output.emitInstruction("mul", register.minus(2), register.minus(2), register.minus(1));
                    break;
                case DIV:
                    output.emitInstruction("div", register.minus(2), register.minus(2), register.minus(1));
                    break;
            }
            register = register.minus(1);
        }

        public void logicOperator(BinaryExpression binaryExpression, String label) {
            binaryExpression.leftOperand.accept(this);
            binaryExpression.rightOperand.accept(this);

            switch (binaryExpression.operator) {
                case EQU:
                    output.emitInstruction("bne", register.minus(2), register.minus(1), label);
                    break;
                case NEQ:
                    output.emitInstruction("beq", register.minus(2), register.minus(1), label);
                    break;
                case GRE:
                    output.emitInstruction("blt", register.minus(2), register.minus(1), label);
                    break;
                case LSE:
                    output.emitInstruction("bgt", register.minus(2), register.minus(1), label);
                    break;
                case GRT:
                    output.emitInstruction("ble", register.minus(2), register.minus(1), label);
                    break;
                case LST:
                    output.emitInstruction("bge", register.minus(2), register.minus(1), label);
                    break;

            }
            register = register.minus(2);
        }

        public void visit(NamedVariable namedVariable) {
            VariableEntry variableEntry = (VariableEntry) localTable.lookup(namedVariable.name);
            output.emitInstruction("add", register, new Register(25), variableEntry.offset);
            //eventuell Ergänzen
            register = register.next();
        }

        public void visit(VariableExpression variableExpression) {
            variableExpression.variable.accept(this);
            output.emitInstruction("ldw", register.minus(1), register.minus(1), 0);
        }

        public void visit(AssignStatement assignStatement) {
            assignStatement.target.accept(this);
            assignStatement.value.accept(this);

            output.emitInstruction("stw", register.minus(1), register.minus(2), 0);
            register = register.minus(2);

        }

        public void visit(ArrayAccess arrayAccess){
            arrayAccess.array.accept(this);
            arrayAccess.index.accept(this);
            //noch zu ergänzen
        }

        public void visit(WhileStatement whileStatement) {
            String startLabel="L"+lblCounter++;
            String endLabel="L"+lblCounter++;

            output.emitLabel(startLabel);
            logicOperator((BinaryExpression) whileStatement.condition,endLabel);
            whileStatement.body.accept(this);
            output.emitInstruction("j",startLabel);
            output.emitLabel(endLabel);
        }

        public void visit(IfStatement ifStatement){

        }





    }

    public CodeGenerator(PrintWriter output, boolean ershovOptimization) {
        this.output = new CodePrinter(output);
        this.ershovOptimization = ershovOptimization;
    }

    /**
     * Emits needed import statements, to allow usage of the predefined functions and sets the correct settings
     * for the assembler.
     */
    private void assemblerProlog() {
        output.emitImport("printi");
        output.emitImport("printc");
        output.emitImport("readi");
        output.emitImport("readc");
        output.emitImport("exit");
        output.emitImport("time");
        output.emitImport("clearAll");
        output.emitImport("setPixel");
        output.emitImport("drawLine");
        output.emitImport("drawCircle");
        output.emitImport("_indexError");
        output.emit("");
        output.emit("\t.code");
        output.emit("\t.align\t4");
    }

    public void generateCode(Program program, SymbolTable table) {
        assemblerProlog();

        //TODO (assignment 6): generate eco32 assembler code for the spl program
        //Visitorklasse


        throw new NotImplemented();
    }
}
