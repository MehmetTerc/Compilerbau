package de.thm.mni.compilerbau.utils;

import de.thm.mni.compilerbau.absyn.Position;
import de.thm.mni.compilerbau.table.Identifier;

/**
 * An exception class, that encapsulates all possible SPL errors.
 * Contains static methods that construct exceptions for specific errors.
 */
public class SplError extends RuntimeException {
    public final Position position;
    public final int errorCode;

    private SplError(Position position, String message, Object... formatArgs) {
        super(String.format(message, formatArgs));
        this.errorCode = 1;
        this.position = position;
    }

    private SplError(int errorCode, Position position, String message, Object... formatArgs) {
        super(String.format(message, formatArgs));
        this.errorCode = errorCode;
        this.position = position;
    }

    //X
    public static SplError SyntaxError(Position position, String token) {
        return new SplError(100, position, "syntax error. Unexpected token '" + token + "'");
    }

    public static SplError UndefinedType(Position position, Identifier name) {
        return new SplError(101, position, "undefined type %s", name);
    }

    //X
    public static SplError NotAType(Position position, Identifier name) {
        return new SplError(102, position, "%s is not a type", name);
    }


    public static SplError RedeclarationAsType(Position position, Identifier name) {
        return new SplError(103, position, "redeclaration of %s as type", name);
    }

    public static SplError MustBeAReferenceParameter(Position position, Identifier name) {
        return new SplError(104, position, "parameter %s must be a reference parameter", name);
    }

    public static SplError RedeclarationAsProcedure(Position position, Identifier name) {
        return new SplError(105, position, "redeclaration of %s as procedure", name);
    }

    public static SplError RedeclarationAsParameter(Position position, Identifier name) {
        return new SplError(106, position, "redeclaration of %s as parameter", name);
    }

    public static SplError RedeclarationAsVariable(Position position, Identifier name) {
        return new SplError(107, position, "redeclaration of %s as variable", name);
    }

    //target und value vergleichen
    //erledigt
    public static SplError AssignmentHasDifferentTypes(Position position) {
        return new SplError(108, position, "assignment has different types");
    }

    //integerabfrage
    public static SplError AssignmentRequiresIntegers(Position position) {
        return new SplError(109, position, "assignment requires integer variable");
    }

    //erledigt
    public static SplError IfConditionMustBeBoolean(Position position) {
        return new SplError(110, position, "'if' test expression must be of type boolean");
    }

    //erledigt
    public static SplError WhileConditionMustBeBoolean(Position position) {
        return new SplError(111, position, "'while' test expression must be of type boolean");
    }

    //CallStatement
    public static SplError UndefinedProcedure(Position position, Identifier name) {
        return new SplError(112, position, "undefined procedure %s", name);
    }
    //CallStatement gucke auf procedure entry, wenn nicht error
    public static SplError CallOfNonProcedure(Position position, Identifier name) {
        return new SplError(113, position, "call of non-procedure %s", name);
    }

    //Callstatement vergleich datentyp mit parametertyp
    public static SplError ArgumentTypeMismatch(Position position, Identifier name, int argumentIndex) {
        return new SplError(114, position, "procedure %s argument %d type mismatch", name, argumentIndex);
    }

    //Variable Call statement
    public static SplError ArgumentMustBeAVariable(Position position, Identifier name, int argumentIndex) {
        return new SplError(115, position, "procedure %s argument %d must be a variable", name, argumentIndex);
    }

    //vergleich anzahl der argumente mit size beim call statement
    public static SplError TooFewArguments(Position position, Identifier name) {
        return new SplError(116, position, "procedure %s called with too few arguments", name);
    }

    //siehe oben
    public static SplError TooManyArguments(Position position, Identifier name) {
        return new SplError(117, position, "procedure %s called with too many arguments", name);
    }

    //operanten vergleichen bei binary
    public static SplError OperatorDifferentTypes(Position position) {
        return new SplError(118, position, "expression combines different types");
    }

    //binary kein integer
    public static SplError ComparisonNonInteger(Position position) {
        return new SplError(119, position, "comparison requires integer operands");
    }

    // siehe oben
    public static SplError ArithmeticOperatorNonInteger(Position position) {
        return new SplError(120, position, "arithmetic operation requires integer operands");
    }

    //Namedvariable mit entry
    public static SplError UndefinedVariable(Position position, Identifier name) {
        return new SplError(121, position, "undefined variable %s", name);
    }

    //Namedvariable
    public static SplError NotAVariable(Position position, Identifier name) {
        return new SplError(122, position, "%s is not a variable", name);
    }

    //arrayaccess datentyp kein arraytypw
    //erledigt
    public static SplError IndexingNonArray(Position position) {
        return new SplError(123, position, "illegal indexing a non-array");
    }

    //arrayaccess datentyp vom index kein integer
    //erledigt
    public static SplError IndexingWithNonInteger(Position position) {
        return new SplError(124, position, "illegal indexing with a non-integer");
    }

    //X
    public static SplError MainIsMissing() {
        return new SplError(125, Position.ERROR_POSITION, "procedure 'main' is missing");
    }

    // tablebuilder checkMain procedureEntry
    public static SplError MainIsNotAProcedure() {
        return new SplError(126, Position.ERROR_POSITION, "'main' is not a procedure");
    }

    //is empty tablebuilder
    public static SplError MainMustNotHaveParameters() {
        return new SplError(127, Position.ERROR_POSITION, "procedure 'main' must not have any parameters");
    }

    //X
    public static SplError IllegalApostrophe(Position position) {
        return new SplError(99, position, "illegal use of apostrophe");
    }

    public static SplError IllegalCharacter(Position position, char character) {
        return new SplError(99, position,
                "Illegal character %s",
                Character.isISOControl(character) ? "0x" + Integer.toString(character, 16) : "'" + character + "'");
    }

    //X
    public static SplError RegisterOverflow() {
        return new SplError(140, Position.ERROR_POSITION, "There are not enough registers to run this program!");
    }
}
