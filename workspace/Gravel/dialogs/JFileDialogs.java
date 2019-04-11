package dialogs;

import io.*;
import main.CONST;
import model.MGraphInterface;
import model.Messages.GraphConstraints;
import model.Messages.GraphMessage;
import model.VGraph;
import model.VGraphInterface;
import model.VHyperGraph;
import view.Gui;
import view.VCommonGraphic;
import view.VGraphic;
import view.VHyperGraphic;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

//import io.MyTikZPictureWriter;

/**
 * JFileDialogs provides all Dialogs concerning loading/saving and the questions for Loading/Saving of Graphs
 * Observes the Changes in the actually edited (Hyper)graph to indicate whether it was saved yet or not.
 * <p>
 * Features
 * - Loading of GravelML (without handling pure mathematical graphs, because there is no algorithm/Dialog for that yet)
 * - Save as and Save in GravelML ith questioning of saving format (pure math or all)
 * - Exports to
 * - PNG
 * - LaTeX	Picture Export
 * - SVG	Export als Vektorgrafik
 * <p>
 * Ideas for further Exports: GraphML, .dot, TikZ,...?
 * Ideas for Imports: GraphML, .dot, GML,...?
 *
 * @author Ronny Bergmann
 */
public class JFileDialogs implements Observer {
  /**
   * A Simple FileFilter that only displays Folders and the files of a given Extension
   *
   * @author Ronny
   */
  public static class SimpleFilter extends FileFilter {
    private String m_description = null;
    private String m_extension = null;

    public SimpleFilter(String extension, String description) {
      m_description = description;
      m_extension = "." + extension.toLowerCase();
    }

    public String getDescription() {
      return m_description;
    }

    public String getStdFileExtension() {
      return m_extension.toLowerCase();
    }

    public boolean accept(File f) {
      if (f == null)
        return false;
      if (f.isDirectory())
        return true;
      return f.getName().toLowerCase().endsWith(getStdFileExtension());
    }
  }

  /**
   * modified JFileChooser to ask before overwriting any file
   *
   * @author Ronny Bergmann
   */
  class JOverwriteCheckFileChooser extends JFileChooser {
    private static final long serialVersionUID = 1L;
    String command = "";

    public JOverwriteCheckFileChooser(String s) {
      super(s);
    }

    public int showSaveDialog(Component parent) {
      command = "save";
      return super.showSaveDialog(parent);
    }

    public void approveSelection() {
      int selection = -1;
      File fold = getSelectedFile();
      String s = ((SimpleFilter) getFileFilter()).getStdFileExtension();
      File f;
      if ((s != null) && (getExtension(fold) == null)) //nur etwa test angegeben
      {
        f = new File(fold.getParent() + "/" + fold.getName() + s);
      } else
        f = fold;
      //if f.getE
      if (command.equalsIgnoreCase("save")) {
        if (getSelectedFile().exists())
          selection = JOptionPane.showConfirmDialog(this, "<html><p>Die Datei existiert bereits. M" + main.CONST.html_oe + "chten Sie die Datei<br>" + f.getName() + "<br>" + main.CONST.html_ue + "berschreiben ?</p></html>"
              , "Datei überschreiben", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (selection == JOptionPane.NO_OPTION)
          return;
        else
          super.approveSelection();
      } else
        super.approveSelection();
      this.setSelectedFile(f);
    }

    private String getExtension(File f) {
      String ext = null;
      String s = f.getName();
      int i = s.lastIndexOf('.');
      if (i > 0 && i < s.length() - 1) {
        ext = s.substring(i + 1).toLowerCase();
      }
      return ext;
    }
  }

  private boolean saveVisual = true;

  private VCommonGraphic vGc;
  private int GraphType;
  private VGraphInterface vG;
  private boolean actualState;

  /**
   * Constructor
   *
   * @param pvg           the actual graph editor Component
   * @param programmstart initialize graphsaved
   */
  public JFileDialogs(VCommonGraphic pvg) {
    GraphType = pvg.getType();
    vGc = pvg;
    if (GraphType == VCommonGraphic.VGRAPHIC) {
      vG = ((VGraphic) vGc).getGraph();
      ((VGraphic) vGc).getGraph().addObserver(this);
    } else {
      vG = ((VHyperGraphic) vGc).getGraph();
      ((VHyperGraphic) vGc).getGraph().addObserver(this);
    }
    actualState = vGc.getGraphHistoryManager().IsGraphUnchanged();
  }

  /**
   * Extracts the extension of a File
   *
   * @param f the File to get the extension from
   * @return the extension as a String
   */
  private String getExtension(File f) {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');
    if (i > 0 && i < s.length() - 1) {
      ext = s.substring(i + 1).toLowerCase();
    }
    return ext;
  }

  private boolean OpenOld(File f) {
    VGraphInterface loadedVGraph = null;
    MGraphInterface loadedMGraph = null;
    String error = "";
    GravelMLReader R = new GravelMLReader(f);
    error = R.checkFile();
    if (error.equals("")) //if okay - load
      error = R.readGraph(); // Graph einlesen, Fehler merken
    loadedVGraph = R.getVGraph();
    loadedMGraph = R.getMGraph();
    if (!error.equals("")) //es liegt ein fehler vor
    {
      String HTMLError = error.replace("\n", "<br/>");
      JOptionPane.showMessageDialog(Gui.getInstance().getParentWindow(), "<html><p>Der Graph konnte nicht geladen werden<br>Fehler :<br>" + HTMLError + "</p></html>", "Fehler beim Laden", JOptionPane.ERROR_MESSAGE);
      return false;
    } //kein Fehler und ein VGraph
    else if (loadedVGraph != null) {
      Gui.getInstance().setVGraph(loadedVGraph);
      GeneralPreferences.getInstance().setStringValue("graph.lastfile", f.getAbsolutePath());
      Gui.getInstance().getParentWindow().setTitle(Gui.WindowName + " - " + f.getName() + "");
      if (GraphType == VCommonGraphic.VGRAPHIC) {
        ((VGraphic) vGc).getGraph().addObserver(this);
        ((VGraphic) vGc).getGraph().pushNotify(new GraphMessage(GraphConstraints.GRAPH_ALL_ELEMENTS, GraphConstraints.REPLACEMENT));
      } else {
        ((VHyperGraphic) vGc).getGraph().addObserver(this);
        ((VHyperGraphic) vGc).getGraph().pushNotify(new GraphMessage(GraphConstraints.HYPERGRAPH_ALL_ELEMENTS, GraphConstraints.REPLACEMENT));
      }
      //Set actual State saved.
      vGc.getGraphHistoryManager().setGraphSaved();
      return true;
    } else if (loadedMGraph != null) {
      main.DEBUG.println(main.DEBUG.LOW, "DEBUG : MGraph geladen, TODO Wizard hier einbauen.");
      JOptionPane.showMessageDialog(Gui.getInstance().getParentWindow(), "<html><p>Die Datei <br><i>" + f.getName() + "</i><br>enth" + main.CONST.html_ae + "lt einen mathematischen Graphen. Diese können bisher nicht weiter verarbeitet werden.</p></html>", "Hinweis", JOptionPane.INFORMATION_MESSAGE);
      //TODO: Wizard
      //Gui.getInstance().getParentWindow().setTitle(Gui.WindowName+" - "+f.getName()+" (math only)");
      //vGc.getVGraph().getMathGraph().addObserver(this);
      //vGc.getVGraph().deleteObserver(this);
      //actualgraphsaved = true; //because only the math part was loaded
      return true;
    } else
      return false;
  }

  /**
   * Loads a Graph from a GravelGraphML Source
   *
   * @return true, if a Graph is loaded, else false
   */
  public boolean Open(File f) {
    VGraphInterface loadedVGraph = null;
    MGraphInterface loadedMGraph = null;
    String error = "";
    GraphMLReader Reader = new GraphMLReader(f, true);
    if ((Reader.WarningOccured()) && (!Reader.ErrorOccured())) {
      int sel = JOptionPane.showConfirmDialog(Gui.getInstance().getParentWindow(),
          "<html><p width='500px' align='center'><b>Beim Laden ist eine Warnung aufgetreten, " +
              "die Datei konnte nicht " + main.CONST.html_ue + "berpr" + main.CONST.html_ue + "ft werden.</b></p>" +
              "<p width=500px>Sie haben entweder keine Verbindung zum Internet oder die Datei ist fehlerhaft.<br>" +
              "Wenn Sie trotzdem den Graphen laden wollen, kann er unvollst" + main.CONST.html_ae + "ndig oder gar komplett leer sein. Im Folgenden die komplette Warnung:<br><br>" +
              "<p width=450px><font size='-1'>" + main.CONST.encodetoHTML(Reader.getWarningMsg()) + "</font></p><br>M" + main.CONST.html_oe + "chten Sie den Graphen trotzdem Laden?</p></html>",
          "Möchten Sie den Graphen trotz Warnung Laden?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
      if (sel == JOptionPane.YES_OPTION)
        Reader = new GraphMLReader(f, false); //Try without validation
      else
        return false; //Do not load;
    }
    if (Reader.ErrorOccured())
      error = Reader.getErrorMsg();
    else {
      loadedVGraph = Reader.getVGraph();
      loadedMGraph = Reader.getMGraph();
    }
    //bei einem der beiden ist ein Fehler aufgetreten
    if (!error.equals("")) //es liegt ein fehler vor
    {
      JOptionPane.showMessageDialog(Gui.getInstance().getParentWindow(),
          "<html><p width=300px>Der Graph konnte nicht geladen werden, da folgender Fehler aufgetreten ist:<br><font size='-1'><br>" + main.CONST.encodetoHTML(error) + "</font></p></html>", "Fehler beim Laden", JOptionPane.ERROR_MESSAGE);
      return false;
    } //kein Fehler und ein VGraph
    else if (loadedVGraph != null) {
      Gui.getInstance().setVGraph(loadedVGraph);
      GeneralPreferences.getInstance().setStringValue("graph.lastfile", f.getAbsolutePath());
      Gui.getInstance().getParentWindow().setTitle(Gui.WindowName + " - " + f.getName() + "");
      if (GraphType == VCommonGraphic.VGRAPHIC) {
        ((VGraphic) vGc).getGraph().addObserver(this);
        ((VGraphic) vGc).getGraph().pushNotify(new GraphMessage(GraphConstraints.GRAPH_ALL_ELEMENTS, GraphConstraints.REPLACEMENT));
      } else {
        ((VHyperGraphic) vGc).getGraph().addObserver(this);
        ((VHyperGraphic) vGc).getGraph().pushNotify(new GraphMessage(GraphConstraints.HYPERGRAPH_ALL_ELEMENTS, GraphConstraints.REPLACEMENT));
      }
      //Set actual State saved.
      vGc.getGraphHistoryManager().setGraphSaved();
      return true;
    } else if (loadedMGraph != null) {
      main.DEBUG.println(main.DEBUG.LOW, "DEBUG : MGraph geladen, TODO Wizard hier einbauen.");
      JOptionPane.showMessageDialog(Gui.getInstance().getParentWindow(), "<html><p>Die Datei <br><i>" + f.getName() + "</i><br>enth" + main.CONST.html_ae + "lt einen mathematischen Graphen. Diese können bisher nicht weiter verarbeitet werden.</p></html>", "Hinweis", JOptionPane.INFORMATION_MESSAGE);
      //TODO: Wizard
      //Gui.getInstance().getParentWindow().setTitle(Gui.WindowName+" - "+f.getName()+" (math only)");
      //vGc.getVGraph().getMathGraph().addObserver(this);
      //vGc.getVGraph().deleteObserver(this);
      //actualgraphsaved = true; //because only the math part was loaded
      return true;
    } else
      return false;
  }

  /**
   * Loads a Graph from a GravelGraphML Source
   *
   * @return true, if a Graph is loaded, else false
   */
  public boolean Open() {
    if (!Gui.getInstance().ApplyChange())
      return false; //do not proceed with loading
    if (!SaveIfUnsafed()) //s.o. aborted
      return false; //Chosen cancel
    JFileChooser fc = new JFileChooser("Öffnen einer Gravel-Datei");
    //Letzten Ordner verwenden
    if (!GeneralPreferences.getInstance().getStringValue("graph.lastfile").equals("$NONE"))
      fc.setCurrentDirectory(new File(GeneralPreferences.getInstance().getStringValue("graph.lastfile")).getParentFile());
    SimpleFilter GraphMLFilter = new SimpleFilter("XML", "GraphML (.xml)"),
        GravelMLFilter = new SimpleFilter("XML", "GravelML (.xml, laden alter Graphen bis v. 0.3)");
    fc.removeChoosableFileFilter(fc.getFileFilter()); //Remove display all
    fc.addChoosableFileFilter(GraphMLFilter);
    fc.addChoosableFileFilter(GravelMLFilter);
    fc.setFileFilter(GraphMLFilter);
    int returnVal = fc.showOpenDialog(Gui.getInstance().getParentWindow());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      if (fc.getFileFilter() == GravelMLFilter)
        return OpenOld(fc.getSelectedFile());
      else if (fc.getFileFilter() == GraphMLFilter)
        return Open(fc.getSelectedFile());
    }
    return false; //Chosen Cancel
  }

  /**
   * Saves a Graph to a GravelGraphML File
   * <p>
   * The File name and Save Modus are extracted from the generalpreferences
   *
   * @return true, if the saving was successful, else false
   */
  public boolean Save() {
    if (!Gui.getInstance().ApplyChange())
      return false; //do not save
    if (GeneralPreferences.getInstance().getStringValue("graph.lastfile").equals("$NONE")) { //Can't save
      return false;
    }
    String filename = GeneralPreferences.getInstance().getStringValue("graph.lastfile");
    File f = new File(filename);
    GraphMLWriter iw = null;
    if (GraphType == VCommonGraphic.VGRAPHIC)
      iw = new GraphMLWriter(((VGraphic) vGc).getGraph()); //Save the actual reference
    else if (GraphType == VCommonGraphic.VHYPERGRAPHIC)
      iw = new GraphMLWriter(((VHyperGraphic) vGc).getGraph()); //Save the actual reference
    else
      return false;
    saveVisual = GeneralPreferences.getInstance().getStringValue("graph.fileformat").equals("visual");
    if (saveVisual) {
      if (iw.saveVisualToFile(f).equals("")) //Saving successfull
      {
        Gui.getInstance().getParentWindow().setTitle(Gui.WindowName + " - " + f.getName());
        //Set actual State saved.
        vGc.getGraphHistoryManager().setGraphSaved();
        return true;
      }
    } else {
      if (iw.saveMathToFile(f).equals("")) //Saving sucessful
      {
        Gui.getInstance().getParentWindow().setTitle(Gui.WindowName + " - " + f.getName() + " (math only)");
        //Set actual State saved.
        vGc.getGraphHistoryManager().setGraphSaved();
        return true;
      }
    }
    return false; //an error occured
  }

  /**
   * Save as a File
   *
   * @return true, if the saving was sucessful
   */
  public boolean SaveAs() {
    if (!Gui.getInstance().ApplyChange())
      return false; //do not save as
    JOverwriteCheckFileChooser fc = new JOverwriteCheckFileChooser("Speichern unter");
    //Last used Directory
    if (!GeneralPreferences.getInstance().getStringValue("graph.lastfile").equals("$NONE"))
      fc.setCurrentDirectory(new File(GeneralPreferences.getInstance().getStringValue("graph.lastfile")).getParentFile());

    SimpleFilter graphml = new SimpleFilter("XML", "GraphML (.xml)");
    fc.removeChoosableFileFilter(fc.getFileFilter()); //Remove display all
    fc.addChoosableFileFilter(graphml);
    saveVisual = GeneralPreferences.getInstance().getStringValue("graph.fileformat").equals("visual");
    fc.setFileFilter(graphml);
    int returnVal = fc.showSaveDialog(Gui.getInstance().getParentWindow());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File f = fc.getSelectedFile();
      GraphMLWriter iw = null;
      if (GraphType == VCommonGraphic.VGRAPHIC)
        iw = new GraphMLWriter(((VGraphic) vGc).getGraph()); //Save the actual reference
      else if (GraphType == VCommonGraphic.VHYPERGRAPHIC)
        iw = new GraphMLWriter(((VHyperGraphic) vGc).getGraph()); //Save the actual reference
      else
        return false;

      SaveAsDialog sad = new SaveAsDialog(Gui.getInstance().getParentWindow());
      if (sad.IsAccepted()) {
        //Dialog visual or not
        saveVisual = sad.IsVisual();
        if (saveVisual) {
          GeneralPreferences.getInstance().setStringValue("graph.fileformat", "visual");
          String error = iw.saveVisualToFile(f);
          if (error.equals("")) {
            GeneralPreferences.getInstance().setStringValue("graph.lastfile", f.getAbsolutePath());
            //Observe VGraph
            if (GraphType == VCommonGraphic.VGRAPHIC)
              ((VGraphic) vGc).getGraph().addObserver(this);
            else if (GraphType == VCommonGraphic.VHYPERGRAPHIC)
              ((VHyperGraphic) vGc).getGraph().addObserver(this);
            Gui.getInstance().getParentWindow().setTitle(Gui.WindowName + " - " + f.getName());
            //Set actual State saved.
            vGc.getGraphHistoryManager().setGraphSaved();
          } else
            main.DEBUG.println(main.DEBUG.LOW, "Error when writing File:" + error);
        } else {
          GeneralPreferences.getInstance().setStringValue("graph.fileformat", "math");
          if (iw.saveMathToFile(f).equals("")) {
            GeneralPreferences.getInstance().setStringValue("graph.lastfile", f.getAbsolutePath());
            //Observe MGraph
            if (GraphType == VCommonGraphic.VGRAPHIC)
              ((VGraphic) vGc).getGraph().addObserver(this);
            else
              ((VHyperGraphic) vGc).getGraph().addObserver(this);
            Gui.getInstance().getParentWindow().setTitle(Gui.WindowName + " - " + f.getName() + " (math only)");
            //Set actual State saved.
            vGc.getGraphHistoryManager().setGraphSaved();
          }
        }
      } //End of SaveAsAccepted
      return true;
    }
    //Chosen Cancel
    return false;
  }

  /**
   * Export an File
   * PNG
   * TeX
   * PNG
   */
  public boolean Export() {
    if (!Gui.getInstance().ApplyChange())
      return false; //do not export
    JFileChooser fc = new JOverwriteCheckFileChooser("Exportieren");
    if (!GeneralPreferences.getInstance().getStringValue("graph.lastfile").equals("$NONE"))
      fc.setCurrentDirectory(new File(GeneralPreferences.getInstance().getStringValue("graph.lastfile")).getParentFile());
    //Wenn man schon nen File hat das Directory davon verwenden
    SimpleFilter png = new SimpleFilter("png", "Portable Network Graphics (.png)");
    SimpleFilter tex = new SimpleFilter("TEX", "LaTeX-Picture-Grafik (.tex)");
    SimpleFilter svg = new SimpleFilter("SVG", "Scalable Vector Graphics (.svg)");
    fc.removeChoosableFileFilter(fc.getFileFilter()); //Remove display all
    fc.addChoosableFileFilter(png);
    fc.addChoosableFileFilter(tex);
    fc.addChoosableFileFilter(svg);
    fc.setFileFilter(png);

    int returnVal = fc.showSaveDialog(Gui.getInstance().getParentWindow());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File f = fc.getSelectedFile();
      if (fc.getFileFilter() == png) {
        if (!CheckExtension(f, png))
          return false;
        ExportPNGDialog esvgd = new ExportPNGDialog(Gui.getInstance().getParentWindow(),
            (vG.getMaxPoint(vGc.getGraphics()).x - vG.getMinPoint(vGc.getGraphics()).x),
            (vG.getMaxPoint(vGc.getGraphics()).y - vG.getMinPoint(vGc.getGraphics()).y));
        if (esvgd.IsAccepted()) {
          PNGWriter iw = new PNGWriter(vGc);
          iw.PNGExport(f, esvgd.getSizeX(), esvgd.getSizeY(), esvgd.isTransparent());
          return true;
        }
      } else if (fc.getFileFilter() == svg) {
        if (!CheckExtension(f, svg))
          return false;
        ExportSVGDialog esvgd = new ExportSVGDialog(Gui.getInstance().getParentWindow(),
            (vG.getMaxPoint(vGc.getGraphics()).x - vG.getMinPoint(vGc.getGraphics()).x),
            (vG.getMaxPoint(vGc.getGraphics()).y - vG.getMinPoint(vGc.getGraphics()).y));
        if (esvgd.IsAccepted()) {
          SVGWriter iw = new SVGWriter(vGc, esvgd.getSizeX());
          iw.saveToFile(f);
          return true;
        }
      } else if (fc.getFileFilter() == tex) {
        if (!CheckExtension(f, tex))
          return false;
        ExportTeXDialog etexd = new ExportTeXDialog(Gui.getInstance().getParentWindow(),
            (vG.getMaxPoint(vGc.getGraphics()).x - vG.getMinPoint(vGc.getGraphics()).x),
            (vG.getMaxPoint(vGc.getGraphics()).y - vG.getMinPoint(vGc.getGraphics()).y));
        if (etexd.IsAccepted()) {
          String type = "";
          if (etexd.IsWholeDocument())
            type = "doc";
          else if (etexd.IsOnlyFigure())
            type = "fig";
          TeXWriter lp;
          if (etexd.IsPlainTeX())
            lp = new LaTeXPictureWriter(vGc, etexd.getSizeX(), type);
          else {
            lp = new MyTikZPictureWriter(vGc, etexd.getSizeX(), type);
          }
          String error = lp.saveToFile(f);
          if (error.equals(""))
            return true;
          JOptionPane.showMessageDialog(Gui.getInstance().getParentWindow(), "<html>Beim Exportieren des Graphen ist folgener Fehler aufgetreten: <br>" + error + "</html>", "Fehler", JOptionPane.ERROR_MESSAGE);
        }
      } else {
        String s = "'." + getExtension(f) + "'";
        if (getExtension(f) == null)
          s = "<i>leer</i>";
        JOptionPane.showMessageDialog(Gui.getInstance().getParentWindow(), "<html>Das Format " + s + " wird nicht unterstützt.<br><br>Unterst" + main.CONST.html_ue + "tzte Formate:<br>-LaTeX (.tex)<br>-PNG (.png)<br>-SVG (.svg)", "Fehler", JOptionPane.ERROR_MESSAGE);
      }
    }
    return false;
  }

  private boolean CheckExtension(File f, FileFilter ff) {
    if (ff.accept(f))
      return true;
    int sel = JOptionPane.showConfirmDialog(Gui.getInstance().getParentWindow(),
        "<html>Die Datei<br><br><i>" + f.getAbsolutePath() + "</i><br><br>hat eine Falsche Endung (." +
            getExtension(f) + ") zum Export in <br><i>" + ff.getDescription() + "</i><br> M" + CONST.html_oe +
            "chten Sie trotzdem exportieren?", "Unbekannte Dateiendung für den Export", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    return sel != JOptionPane.NO_OPTION;
  }

  /**
   * Get the actual status of the graph
   *
   * @return true if the current Graph is saved in a file, else false
   */
  public boolean isGraphSaved() {
    return vGc.getGraphHistoryManager().IsGraphUnchanged();
  }

  /**
   * Ask on File->New or File->Open to Save an unsaved Graph
   * and return, whether it was saved or the action was canceled.
   *
   * @return true if the graph was saved or Saving was denied, false if the action was canceled
   */
  public boolean SaveIfUnsafed() {
    boolean existsNode = false;
    if (vG.getType() == VGraphInterface.GRAPH)
      existsNode = ((VGraph) vG).getMathGraph().modifyNodes.cardinality() > 0;
    else if (vG.getType() == VGraphInterface.HYPERGRAPH)
      existsNode = ((VHyperGraph) vG).getMathGraph().modifyNodes.cardinality() > 0;

    if ((!vGc.getGraphHistoryManager().IsGraphUnchanged()) && (existsNode)) //saved/no changes
    {
      if (GeneralPreferences.getInstance().getStringValue("graph.lastfile").equals("$NONE")) { //SaveAs anbieten
        int sel = JOptionPane.showConfirmDialog(Gui.getInstance().getParentWindow(), "<html><p>Der aktuelle Graph ist nicht gespeichert worden.<br>M" + main.CONST.html_oe + "chten Sie den Graph noch speichern ?</p></html>"
            , "Graph nicht gespeichert", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (sel == JOptionPane.CANCEL_OPTION)
          return false;
        if (sel == JOptionPane.YES_OPTION)
          SaveAs();
      } else //Sonst Save anbieten
      {
        File f = new File(GeneralPreferences.getInstance().getStringValue("graph.lastfile"));
        int sel = JOptionPane.showConfirmDialog(Gui.getInstance().getParentWindow(), "<html><p>Die letzten " + main.CONST.html_Ae + "nderungen an<br><i>" + f.getName() + "</i><br>wurden nicht gespeichert.<br>M" + main.CONST.html_oe + "chten Sie diese jetzt speichern ?</p></html>"
            , "Graph nicht gespeichert", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (sel == JOptionPane.CANCEL_OPTION)
          return false;
        if (sel == JOptionPane.YES_OPTION)
          Save();
      }
    }
    //No one aborted so return true
    return true;
  }

  private String buildNewGraph() {
    String[] selections = {"Graph", "Hypergraph"};
    int sel;
    if (GeneralPreferences.getInstance().getBoolValue("graph.new_with_graph"))
      sel = 0;
    else
      sel = 1;
    return (String) JOptionPane.showInputDialog(Gui.getInstance().getParentWindow(), "<html>Bitte Graphentyp w" + main.CONST.html_ae + "hlen:</html>",
        "Neue Graphen erstellen", JOptionPane.QUESTION_MESSAGE, null,
        selections, selections[sel]);
  }

  /**
   * Create a New Graph in the GUI
   */
  public void NewGraph() {
    if (!Gui.getInstance().ApplyChange())
      return; //s.o. aborted the apply
    if (!SaveIfUnsafed()) //s.o. aborted
      return;
    VGraphInterface vg;
    String newgraph = buildNewGraph();
    if (newgraph == null)
      newgraph = "";
    if (newgraph.equals("Graph"))
      vg = new VGraph(GeneralPreferences.getInstance().getBoolValue("graph.directed"), GeneralPreferences.getInstance().getBoolValue("graph.allowloops"), GeneralPreferences.getInstance().getBoolValue("graph.allowmultiple"));
    else if (newgraph.equals("Hypergraph"))
      vg = new VHyperGraph();
    else
      return;
    GeneralPreferences.getInstance().setStringValue("graph.lastfile", "$NONE");
    //Deactivate HistoryStuff
    Gui.getInstance().setVGraph(vg); //This should kill us if the graphtype changed
    //Reset (and with that reactivate History
    Gui.getInstance().getParentWindow().setTitle(Gui.WindowName);
  }

  /**
   * Handle Graph Updates and check, whether a loaded graph is still saved
   */
  public void update(Observable arg0, Object arg1) {
    if (this.actualState != vGc.getGraphHistoryManager().IsGraphUnchanged()) //State Changed
      actualState = vGc.getGraphHistoryManager().IsGraphUnchanged();
    else
      return; //Don't update title too often
    String newtitle = "";
    if (actualState) //Graph is unchanged again
    {
      if (!GeneralPreferences.getInstance().getStringValue("graph.lastfile").equals("$NONE")) {
        if (saveVisual)
          newtitle = Gui.WindowName + " - " + (new File(GeneralPreferences.getInstance().getStringValue("graph.lastfile")).getName()) + "";
        else
          newtitle = Gui.WindowName + " - " + (new File(GeneralPreferences.getInstance().getStringValue("graph.lastfile")).getName()) + " (math only)";
      } else
        newtitle = Gui.WindowName;
      if (System.getProperty("os.name").toLowerCase().contains("mac")) //Back to X as close
        Gui.getInstance().getParentWindow().getRootPane().putClientProperty("Window.documentModified", Boolean.FALSE);
    } else //Graph not saved
    {
      if (!GeneralPreferences.getInstance().getStringValue("graph.lastfile").equals("$NONE")) {
        if (saveVisual)
          newtitle = Gui.WindowName + " - " + (new File(GeneralPreferences.getInstance().getStringValue("graph.lastfile")).getName()) + "*";
        else
          newtitle = Gui.WindowName + " - " + (new File(GeneralPreferences.getInstance().getStringValue("graph.lastfile")).getName()) + "* (math only)";
      } else
        newtitle = Gui.WindowName;
      if (System.getProperty("os.name").toLowerCase().contains("mac")) //Back to Circle on CLose
        Gui.getInstance().getParentWindow().getRootPane().putClientProperty("Window.documentModified", Boolean.TRUE);

    }
    Gui.getInstance().getParentWindow().setTitle(newtitle);
  }

}
