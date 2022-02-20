package de.thm.mni.compilerbau.phases._04a_tablebuild;

import de.thm.mni.compilerbau.absyn.*;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.absyn.visitor.Visitable;
import de.thm.mni.compilerbau.absyn.visitor.Visitor;
import de.thm.mni.compilerbau.table.*;
import de.thm.mni.compilerbau.types.ArrayType;
import de.thm.mni.compilerbau.types.Type;
import de.thm.mni.compilerbau.utils.NotImplemented;
import de.thm.mni.compilerbau.utils.SplError;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//Aufgabe Tabelle erstellen, worauf der SPL prüft, ob es bei den Eingaben soweit passt

/**
 * This class is used to create and populate a {@link SymbolTable} containing entries for every symbol in the currently
 * compiled SPL program.
 * Every declaration of the SPL program needs its corresponding entry in the {@link SymbolTable}.
 * <p>
 * Calculated {@link Type}s can be stored in and read from the dataType field of the {@link Expression},
 * {@link TypeExpression} or {@link Variable} classes.
 */
public class TableBuilder extends DoNothingVisitor {
    private final boolean showTables;

    public TableBuilder(boolean showTables) {
        this.showTables = showTables;
    }

    public SymbolTable buildSymbolTable(Program program) {
        //TODO (assignment 4a): Initialize a symbol table with all predefined symbols and fill it with user-defined symbols
        SymbolTable globalTable=TableInitializer.initializeGlobalTable();
        TableBuilderVisitor tableBuilderVisitor=new TableBuilderVisitor((globalTable));
        program.accept(tableBuilderVisitor);
        Entry mainError = globalTable.lookup(new Identifier("main"), SplError.MainIsMissing());

        if (!(mainError instanceof ProcedureEntry)) {
            throw SplError.MainIsNotAProcedure();
        } else {
            if (!((ProcedureEntry) mainError).parameterTypes.isEmpty()) {
                throw SplError.MainMustNotHaveParameters();
            }
        }
        return globalTable;


    }


    /**
     * Prints the local symbol table of a procedure together with a heading-line
     * NOTE: You have to call this after completing the local table to support '--tables'.
     *
     * @param name  The name of the procedure
     * @param entry The entry of the procedure to print
     */
    private static void printSymbolTableAtEndOfProcedure(Identifier name, ProcedureEntry entry) {
        System.out.format("Symbol table at end of procedure '%s':\n", name);
        System.out.println(entry.localTable.toString());
    }

    private class TableBuilderVisitor extends DoNothingVisitor {
        private SymbolTable globalTable;
        private SymbolTable localTable;

        public TableBuilderVisitor(SymbolTable symbolTable) {
            this.globalTable = symbolTable;
        }

        @Override
        public void visit(ArrayTypeExpression arrayTypeExpression) {
            arrayTypeExpression.baseType.accept(this);
            arrayTypeExpression.dataType = new ArrayType(
                    arrayTypeExpression.baseType.dataType, arrayTypeExpression.arraySize);
        }

        @Override
        public void visit(NamedTypeExpression namedTypeExpression) {
            TypeEntry entry = (TypeEntry) globalTable.lookup(namedTypeExpression.name);
            namedTypeExpression.dataType = entry.type;
        }

        @Override
        public void visit(ParameterDeclaration parameterDeclaration) {
            parameterDeclaration.typeExpression.accept(this);
            if (parameterDeclaration.typeExpression.dataType instanceof ArrayType && !parameterDeclaration.isReference) {
                throw SplError.MustBeAReferenceParameter(parameterDeclaration.position, parameterDeclaration.name);
            }

            localTable.enter(parameterDeclaration.name,
                    new VariableEntry(parameterDeclaration.typeExpression.dataType, parameterDeclaration.isReference),
                    SplError.RedeclarationAsParameter(parameterDeclaration.position, parameterDeclaration.name));
        }

        @Override
        public void visit(ProcedureDeclaration procedureDeclaration) {
            List<ParameterType> paraType = new ArrayList<>();
            localTable = new SymbolTable(globalTable);
            procedureDeclaration.parameters.forEach(n -> {
                n.accept(this);
                paraType.add(new ParameterType(n.typeExpression.dataType, n.isReference));
            });
            procedureDeclaration.variables.forEach((m -> m.accept(this)));
            globalTable.enter(procedureDeclaration.name, new ProcedureEntry(localTable, paraType));
            printSymbolTableAtEndOfProcedure(procedureDeclaration.name, new ProcedureEntry(localTable, paraType));
        }

        @Override
        public void visit(Program program) {
            program.declarations.forEach(d -> d.accept(this));
        }

        @Override
        public void visit(TypeDeclaration typeDeclaration) {
            typeDeclaration.typeExpression.accept(this);
            globalTable.enter(typeDeclaration.name, new TypeEntry(typeDeclaration.typeExpression.dataType));
        }

        @Override
        public void visit(VariableDeclaration variableDeclaration) {
            variableDeclaration.typeExpression.accept(this);
            localTable.enter(variableDeclaration.name, new VariableEntry(variableDeclaration.typeExpression.dataType, false), SplError.RedeclarationAsVariable(variableDeclaration.position, variableDeclaration.name));
        }

    }

}
