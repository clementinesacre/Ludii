package app.display.dialogs.visual_editor.model;

import app.display.dialogs.visual_editor.LayoutManagement.NodePlacementRoutines;
import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;
import app.display.dialogs.visual_editor.recs.display.ProgressBar;
import app.display.dialogs.visual_editor.view.MainFrame;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.editor.EditorPanel;
import compiler.Arg;
import compiler.ArgClass;
import grammar.Grammar;
import main.Constants;
import main.grammar.*;
import main.options.UserSelections;
import other.GameLoader;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Pattern;

public class GameParser
{

    public static void ParseFileToGraph(File file, IGraphPanel graphPanel, ProgressBar progressBar)
    {
        // #1 file to string
        StringBuilder sb = new StringBuilder();
        try (final BufferedReader rdr = new BufferedReader(new FileReader(file.getAbsolutePath())))
        {
            String line;
            while ((line = rdr.readLine()) != null)
                sb.append(line + "\n");
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        progressBar.updateProgress(1);
        // creating a call tree from game description
        // #2 file to description
        Description test_desc = new Description(sb.toString());
        progressBar.updateProgress(2);
        // #3 expand and parse the description
        parser.Parser.expandAndParse(test_desc, new UserSelections(new ArrayList<>()),new Report(),false);
        progressBar.updateProgress(3);
        // #4 create token tree
        Token tokenTree_test = new Token(test_desc.expanded(), new Report());
        Grammar gm = grammar.Grammar.grammar();
        progressBar.updateProgress(4);
        // #5 create from token
        final ArgClass rootClass = (ArgClass) Arg.createFromToken(grammar.Grammar.grammar(), tokenTree_test);
        assert rootClass != null;
        progressBar.updateProgress(5);
        // #6 match symbols
        progressBar.updateProgress(6);
        rootClass.matchSymbols(gm, new Report());
        // #7 compile a call tree
        progressBar.updateProgress(7);
        // Attempt to compile the game
        Class<?> clsRoot = null;
        try
        {
            clsRoot = Class.forName("game.Game");
        } catch (final ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        // Create call tree with dummy root
        Call callTree = new Call(Call.CallType.Null);
        final Map<String, Boolean> hasCompiled = new HashMap<>();
        rootClass.compile(clsRoot, (-1), new Report(), callTree, hasCompiled);
        // #8 constructing a graph from call tree
        constructGraph(callTree.args().get(0), 0, null, -1, null, graphPanel.graph());
        progressBar.updateProgress(8);
    }

    /**
     * Prints out all the necessary information to construct ludeme graph
     * @param c parent call
     * @param d depth
     * @param graph graph in operation
     */
    private static Object constructGraph(Call c, int d, LudemeNode parent, int collectionIndex, NodeArgument creatorArgument, DescriptionGraph graph)
    {
        List<Call> cArgs = c.args();
        int matched = 0;
        int counter = 0;
        switch (c.type())
        {
            case Array:
                // Apply method for the arguments of c
                Object[] objects = new Object[cArgs.size()];
                for (int i = 0; i < cArgs.size(); i++)
                {
                    objects[i] = constructGraph(cArgs.get(i), d+1, parent, i, creatorArgument, graph);
                }
                return objects;
            case Terminal:
                return c.object();
            case Class:
                // TODO: debug this!
                Symbol ludemeSymbol = c.symbol();
                List<Clause> rhsList = c.symbol().rule().rhs();
                Clause currentClause = null;
                for (Clause clause : rhsList)
                {
                    if (clause.args() != null && c.args().size() == clause.args().size())
                    {
                        List<ClauseArg> iRhs = clause.args();
                        for (int j = 0; j < cArgs.size(); j++)
                        {
                            Symbol s = cArgs.get(j).symbol();
                            if (s != null)
                            {
                                if (s.returnType().token().equals(
                                        iRhs.get(j).symbol().returnType().token()))
                                {
                                    counter++;
                                }
                            }

                        }
                        if (counter > matched || rhsList.size() == 1)
                        {
                            matched = counter;
                            counter = 0;
                            currentClause = clause;
                        }
                    }
                }

                // creating ludeme node from call class
                LudemeNode ln = new LudemeNode(ludemeSymbol, NodePlacementRoutines.DEFAULT_X_POS,
                        NodePlacementRoutines.DEFAULT_Y_POS);
                ln.setCreatorArgument(creatorArgument);
                Handler.addNode(graph, ln);
                // setting up current clause constructor
                Handler.updateCurrentClause(graph, ln, currentClause);
                // providing argument to the ludeme node
                int orGroup = 0;
                int orGroupFinished = -1;
                // node argument cursor
                int i = 0;
                // clause argument cursor
                int j = 0;
                Object input = null;
                while (i < ln.providedInputsMap().size())
                {
                    // check if input for orGroup was produced
                    if (currentClause.args().get(j).orGroup() == orGroupFinished)
                    {
                        j++;
                    }
                    else
                    {
                        Call call = cArgs.get(j);
                        input = constructGraph(call, d+1, ln, -1, ln.currentNodeArguments().get(i), graph);
                        Handler.updateInput(graph, ln, ln.currentNodeArguments().get(i), input);

                        if (currentClause.args().get(j).orGroup() > orGroup)
                        {
                            orGroup = currentClause.args().get(j).orGroup();
                            // check if orGroup is fully iterated
                            if (j - 1 >= 0
                                    && currentClause.args().get(j).orGroup() != currentClause.args().get(j-1).orGroup())
                            {
                                i++;
                            }
                            // check if one of inputs of orGroup was given already
                            else if (input != null)
                            {
                                i++;
                                orGroupFinished = orGroup;
                            }
                        }
                        else
                        {
                            i++;
                        }
                        j++;
                    }
                }
                // add edge to graph model
                if (parent != null)
                {
                    if (collectionIndex == -1)
                        Handler.addEdge(graph, parent, ln, ln.creatorArgument());
                    else
                        Handler.addEdge(graph, parent, ln, ln.creatorArgument(), collectionIndex);
                }

                return ln;

            default:
                return null;
        }
    }


    private static StringBuilder descriptionToString(String gamePath)
    {
        final StringBuilder sb = new StringBuilder();

        String path = gamePath.replaceAll(Pattern.quote("\\"), "/");
        path = path.substring(path.indexOf("/lud/"));

        InputStream in = GameLoader.class.getResourceAsStream(path);
        try (final BufferedReader rdr = new BufferedReader(new InputStreamReader(in, "UTF-8")))
        {
            String line;
            while ((line = rdr.readLine()) != null)
                sb.append(line + "\n");
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        return sb;
    }

}