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

    private class CodeGeneratorVisitor extends DoNothingVisitor {
        SymbolTable localTable;
        SymbolTable globalTable;
        int lblCounter = 0;
        CodePrinter output;

        public CodeGeneratorVisitor(SymbolTable symbolTable,CodePrinter output) {
            this.globalTable = symbolTable;
            this.output=output;
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

        public void visit(ArrayAccess arrayAccess) {
            arrayAccess.array.accept(this);
            arrayAccess.index.accept(this);

            output.emitInstruction("add", register, new Register(0), ((ArrayType) arrayAccess.array.dataType).arraySize);
            output.emitInstruction("bgeu", register.minus(1), register, "_indexError");
            output.emitInstruction("mul", register.minus(1), register.minus(1), arrayAccess.index.dataType.byteSize);
            output.emitInstruction("add", register.minus(2), register.minus(2), register.minus(1));
            register = register.minus(1);
        }

        public void visit(WhileStatement whileStatement) {
            String startLabel = "L" + lblCounter++;
            String endLabel = "L" + lblCounter++;

            output.emitLabel(startLabel);
            logicOperator((BinaryExpression) whileStatement.condition, endLabel);
            whileStatement.body.accept(this);
            output.emitInstruction("j", startLabel);
            output.emitLabel(endLabel);
        }

        public void visit(IfStatement ifStatement) {
            if (ifStatement.elsePart instanceof EmptyStatement) {
                String endlbl = "L" + lblCounter++;
                logicOperator((BinaryExpression) ifStatement.condition, endlbl);
                ifStatement.thenPart.accept(this);
                output.emitLabel(endlbl);
            } else {
                String startLbl = "L" + lblCounter++;
                String endLbl = "L" + lblCounter++;

                logicOperator((BinaryExpression) ifStatement.condition, endLbl);
                output.emitLabel(startLbl);
                ifStatement.elsePart.accept(this);
                output.emitLabel(endLbl);
            }
        }

        public void visit(CallStatement callStatement) {
            ProcedureEntry procedureEntry = (ProcedureEntry) localTable.lookup((callStatement.procedureName));
            for (int i = 0; i < callStatement.arguments.size(); i++) {
                if (procedureEntry.parameterTypes.get(i).isReference) {
                    VariableExpression variableExpression = (VariableExpression) callStatement.arguments.get(i);
                    variableExpression.variable.accept(this);
                } else {
                    callStatement.arguments.get(i).accept(this);
                }

                output.emitInstruction("stw", register.minus(1), new Register(29), procedureEntry.parameterTypes.get(i).offset);
                register = register.minus(1);
            }
            output.emitInstruction("jal", callStatement.procedureName.toString());
        }

        public void visit(ProcedureDeclaration procedureDeclaration) {
            ProcedureEntry procedureEntry = (ProcedureEntry) globalTable.lookup(procedureDeclaration.name);
            output.emitExport(procedureDeclaration.name.toString());
            output.emitLabel(procedureDeclaration.name.toString());
            localTable = procedureEntry.localTable;
            output.emitInstruction("sub", new Register(29), new Register(29), procedureEntry.stackLayout.frameSize(), "allocate frame");
            output.emitInstruction("stw", new Register(25), new Register(29), procedureEntry.stackLayout.oldFramePointerOffset(), "save old FP");
            output.emitInstruction("add", new Register(25), new Register(29), procedureEntry.stackLayout.frameSize(), "new FP");
            if (!procedureEntry.stackLayout.isLeafProcedure()) {
                output.emitInstruction("stw", new Register(31), new Register(25), procedureEntry.stackLayout.oldReturnAddressOffset(), "save old return register");
            }
            procedureDeclaration.body.forEach(n->n.accept(this));
            if(!procedureEntry.stackLayout.isLeafProcedure()){
                output.emitInstruction("ldw", new Register(31), new Register(25), procedureEntry.stackLayout.oldFramePointerOffset(), "load return register");
            }
            output.emitInstruction("ldw",new Register(25),new Register(29),procedureEntry.stackLayout.oldFramePointerOffset(), "restore FP" );
            output.emitInstruction("add",new Register(29), new Register(29),procedureEntry.stackLayout.frameSize(),"release Frame");
            output.emitInstruction("jr",new Register(31),"return to Adress");
        }

        public void visit(Program program){
            program.declarations.forEach(pd -> pd.accept(this));
        }


    }

    /**
     * Initializes the code generator.
     *
     * @param output             The PrintWriter to the output file.
     * @param ershovOptimization Whether the ershov register optimization should be used (--ershov)
     */

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
        CodeGeneratorVisitor codeGeneratorVisitor = new CodeGeneratorVisitor(table,output);
        program.accept(codeGeneratorVisitor);
        //TODO (assignment 6): generate eco32 assembler code for the spl program
    }
}
