# README

## MEMBERS
| Name           | Github |
| -------------- | -------------- | 
| Henrique Sousa (Self) |  https://github.com/henriquecscode  | 
| Mateus Silva   | https://github.com/lessthelonely    | 
| Melissa Silva  | https://github.com/melisilva    |  
| Isabel Vieira  |    | 

## GLOBAL GRADE OF THE PROJECT
19

## SUMMARY
This project aimed to develop a compiler capable of generating valid Java Virtual Machine instructions from .jmm files into Jasmin, which could, in turn, be translated into Java Bytecodes by the Jasmin assembler.
The discriminated phases in the compiler are: syntactical analysis (with AST generation), semantic analysis (visiting the AST), AST-to-OLLIR translation and OLLIR-to-Jasmin translation.

The tool also includes some optimization steps, such as simple dead code elimination, better JVM instructions and constant folding.


## SEMANTIC ANALYSIS
* Type Checking
  * Operands of the same operation must have the same type;
  * Assignee type must match the assigned's;
  * Boolean operations only have boolean operands;
  * Every time the type isn't explicitly known, we use a general type;
* Expressions
  * Operator ! must have a boolean;
  * Arithmetic operators must have integer operands;
  * Short-circuit evaluation is avoided so all potential errors are reported at once (if an expression has an error in its beginning portion, it'll still be visited in full);
* Conditions
  * While conditions must be boolean;
  * If conditions must be boolean;
* Array Access
  * Array access performed only on arrays;
  * Array access index must be an integer;
* Method Calls
  * Upon call, object exists and contains method;
    * For imports, it assumes the method exists (general type is used);
    * For undeclared methods in classes with a super class ("extends"), it's assumed these exist in the super class (general type is used);
  * Number and type of parameters used in call match the declaration;
  * Return statement match declared return type;
  * Chained function calls go through the same verifications (general type MAY be used);

## CODE GENERATION
The code is generated to the folder where the program is run.
It also generates a folder for each class inside the compiled folder.
This folder includes the .j file, the .ollir file, the .json file, which contains the AST (Abstract Syntax Tree) and the .log file, which contains the compiler execution output, including all the compiling stages.

These stages are:
1. JMM code parsing and AST generation;
2. Semantic Analysis and Symbol Table creation;
   1. Simple Dead Code Removal;
   2. Constant Folding;
3. OLLIR Generation
   1. AST traversing;
   2. Constant Propagation with CFG generation;
4. Jasmin Generation 
   1. Selection of Most Efficient Instructions;

## PROS
* Easy-to-Understand Code Optimizations:
    * Simple dead code elimination;
    * Better JVM instructions; 
    * Constant folding;
* Inline comments are supported;
* Custom tests made to test special edge cases;
* Constant propagation;
* Generation of CFG and Gen and Kill Sets;
* An easy-to-understand semantic analysis implementation;
  * Custom-made reports with useful information:
    * Variable names, lines and columns;
* The ! operator is implemented;

## CONS
The one obvious answer is lacking register allocation, although since we took the time to create the CFG, gen and kill sets we think our code is suitable to add this optimization in a easier fashion than it would be otherwise.

Beyond that, as our vision of the project is incredibly skewed and we're quite familiar with the tool, when we tried to give more answers, we came up with nothing.
We'd like to state we've been prioritizing making a functional tool we're proud of, and we believe we've succeeded at that.

We're not saying our project is perfect or flawless, but merely stating we have way too close a view of it to be able to come up with more valid answers without pulling them out of a hat.  We trust the teacher's point of view might yield a more conclusive answer to this question.

# Compilers Project

For this project, you need to install [Java](https://jdk.java.net/), [Gradle](https://gradle.org/install/), and [Git](https://git-scm.com/downloads/) (and optionally, a [Git GUI client](https://git-scm.com/downloads/guis), such as TortoiseGit or GitHub Desktop). Please check the [compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html) for Java and Gradle versions.

## Project setup

There are three important subfolders inside the main folder. First, inside the subfolder named ``javacc`` you will find the initial grammar definition. Then, inside the subfolder named ``src`` you will find the entry point of the application. Finally, the subfolder named ``tutorial`` contains code solutions for each step of the tutorial. JavaCC21 will generate code inside the subfolder ``generated``.

## Compile and Running

To compile and install the program, run ``gradle installDist``. This will compile your classes and create a launcher script in the folder ``./build/install/comp2022-7b/bin``. For convenience, there are two script files, one for Windows (``comp2022-7b.bat``) and another for Linux (``comp2022-7b``), in the root folder, that call tihs launcher script.

After compilation, a series of tests will be automatically executed. The build will stop if any test fails. Whenever you want to ignore the tests and build the program anyway, you can call Gradle with the flag ``-x test``.

## Test

To test the program, run ``gradle test``. This will execute the build, and run the JUnit tests in the ``test`` folder. If you want to see output printed during the tests, use the flag ``-i`` (i.e., ``gradle test -i``).
You can also see a test report by opening ``./build/reports/tests/test/index.html``.

## Checkpoint 1
For the first checkpoint the following is required:

1. Convert the provided e-BNF grammar into JavaCC grammar format in a .jj file
2. Resolve grammar conflicts, preferably with lookaheads no greater than 2
3. Include missing information in nodes (i.e. tree annotation). E.g. include the operation type in the operation node.
4. Generate a JSON from the AST

### JavaCC to JSON
To help converting the JavaCC nodes into a JSON format, we included in this project the JmmNode interface, which can be seen in ``src-lib/pt/up/fe/comp/jmm/ast/JmmNode.java``. The idea is for you to use this interface along with the Node class that is automatically generated by JavaCC (which can be seen in ``generated``). Then, one can easily convert the JmmNode into a JSON string by invoking the method JmmNode.toJson().

Please check the JavaCC tutorial to see an example of how the interface can be implemented.

### Reports
We also included in this project the class ``src-lib/pt/up/fe/comp/jmm/report/Report.java``. This class is used to generate important reports, including error and warning messages, but also can be used to include debugging and logging information. E.g. When you want to generate an error, create a new Report with the ``Error`` type and provide the stage in which the error occurred.


### Parser Interface

We have included the interface ``src-lib/pt/up/fe/comp/jmm/parser/JmmParser.java``, which you should implement in a class that has a constructor with no parameters (please check ``src/pt/up/fe/comp/CalculatorParser.java`` for an example). This class will be used to test your parser. The interface has a single method, ``parse``, which receives a String with the code to parse, and returns a JmmParserResult instance. This instance contains the root node of your AST, as well as a List of Report instances that you collected during parsing.

To configure the name of the class that implements the JmmParser interface, use the file ``config.properties``.

### Compilation Stages

The project is divided in four compilation stages, that you will be developing during the semester. The stages are Parser, Analysis, Optimization and Backend, and for each of these stages there is a corresponding Java interface that you will have to implement (e.g. for the Parser stage, you have to implement the interface JmmParser).


### config.properties

The testing framework, which uses the class TestUtils located in ``src-lib/pt/up/fe/comp``, has methods to test each of the four compilation stages (e.g., ``TestUtils.parse()`` for testing the Parser stage).

In order for the test class to find your implementations for the stages, it uses the file ``config.properties`` that is in root of your repository. It has four fields, one for each stage (i.e. ``ParserClass``, ``AnalysisClass``, ``OptimizationClass``, ``BackendClass``), and initially it only has one value, ``pt.up.fe.comp.SimpleParser``, associated with the first stage.

During the development of your compiler you will update this file in order to setup the classes that implement each of the compilation stages.
