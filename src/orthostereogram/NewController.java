/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerUnpluggedException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Ellipse2D;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;



/**
 *
 * @author Fred
 */
public class NewController extends JFrame implements WindowListener {

    /**
     * Creates new form NewController
     */
    
    Image imgBR, imgRB ;
    static public Image tinyTrophy ;
    static int DEFAULT_IMG_SIZE = 600 ;
    static public int imgSize = DEFAULT_IMG_SIZE ; //only odd sizes are allowed for stereograms
    int numberOfScreens = 1 ;
    int screenHeight ;
    static public JFrame currentFrame ;
    static GraphicsEnvironment graphicsEnv ;
    static GraphicsDevice[] screenDevices ; 
    static GraphicsDevice secondaryScreenDevice ;
    
    //Just to initiate ?
    SlideStereogramView slide ;
    ImageStereogramView image ;
    ClassicStereogramView classic ;
    DoubleStereogram doubleSt ;
    AccommodationJob accommodation ;
    
    //Calibration faite ?
    boolean isCalibrated = false ;
    
    //Graphique
    ChartPanel chartPanel ;
    XYSeries xySerieMax, xySerieMin ;
    
    //XBOX
    static public boolean xboxConnected = false ;
    static boolean glfwInit = false ;
    public ControllerManager controllers;
    
    public NewController() {
        
        Toolkit.getDefaultToolkit().setDynamicLayout(false); 
        setLayout(null);
        initComponents();
        this.addWindowListener(this);
        jUnit.setText("\u0394");jUnit2.setText("\u0394");jUnit3.setText("\u0394");jUnit4.setText("\u0394");
        jUnit6.setText("\u0394"); jUnit7.setText("\u0394");
        
        //AutoConnect
        if (OrthoStereogram.master) connected () ;
        else {
            AutoConnect auto = new AutoConnect () ;
            auto.start () ;
        }
        
        //Image du trophé
        tinyTrophy = getToolkit().getImage(getClass().getResource("/Ressources/trophy-small.png"));
        
        //image Lunettes 3D (inversées)
        imgRB = getToolkit().getImage(getClass().getResource("/Ressources/3d-BR.png"));
        imgBR = getToolkit().getImage(getClass().getResource("/Ressources/3d-RB.png"));
        if (OrthoStereogram.BR_glasses) jImg3D.setIcon(new ImageIcon(imgBR));
        else jImg3D.setIcon(new ImageIcon(imgRB));
        jImg3D.setText("");
        
        //image xbox
        /*Image imgXBOX = getToolkit().getImage(getClass().getResource("/Ressources/xbox-icon64.png"));
        jImgXBOX.setIcon(new ImageIcon(imgXBOX)); jImgXBOX.setText("");*/
       
        //Activités
        //jActivity.
        
        //Divers
        setTitle ("StéréoVergences (F. Maillet - "+OrthoStereogram.VERSION+")") ;
        this.getContentPane().setBackground(Color.CYAN);
        this.setResizable(false);
        
        //Random jumps tootip
        jRandomJumps.setToolTipText("pas opérationnel");        
        
        //List of Screens ?
        try {
            graphicsEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice primaryScreenDevice = graphicsEnv.getDefaultScreenDevice();
            screenDevices = graphicsEnv.getScreenDevices();
            numberOfScreens = screenDevices.length;
            jScreens.removeAllItems();
            for (int i=0; i<numberOfScreens; i++) {
                if (screenDevices[i].isFullScreenSupported()) {
                    if (!primaryScreenDevice.equals(screenDevices[i])) {
                        jScreens.addItem(String.valueOf(i) + ": Projecteur");
                        //else jScreens.insertItemAt(String.valueOf(i) + ": Projecteur", 0);
                    }
                    else
                        jScreens.addItem(String.valueOf(i) + ": Principal");
                }
            }
        } catch (HeadlessException e) { }
        jScreens.setToolTipText("pas opérationnel");
        
        //Check for secondary screen ?
        GraphicsDevice primaryScreenDevice = graphicsEnv.getDefaultScreenDevice();
        for (GraphicsDevice screenDevice : screenDevices) {
            if (!primaryScreenDevice.equals(screenDevice)) {
                secondaryScreenDevice = screenDevice;
                break;
            }
        }
        if (secondaryScreenDevice == null) {
            secondaryScreenDevice = primaryScreenDevice;
        }
        //Check for setFullScreenMode
        if (!secondaryScreenDevice.isFullScreenSupported()) jScreens.setEnabled(false);
        
        //Hauteur en pixels de l'écran ?
        screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height ;
 
    }
    
    public void initController () {
        
        //Si on a la xbox
        //XBOX
        controllers = new ControllerManager();
        controllers.initSDLGamepad();
        //int nControllers = controllers.getNumControllers();
        xboxConnected = (controllers.getNumControllers() > 0); 
        jImgXBOX.setEnabled(xboxConnected);
        
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
          @Override
          public void run() {
            controllers.update();
            xboxConnected = (controllers.getNumControllers() > 0); 
            jImgXBOX.setEnabled(xboxConnected);
          }
        }, 0, 5, TimeUnit.SECONDS);
                
        //On redimensionne le controller
        this.setSize(900, 700);
        //Données du graphique
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        xySerieMax = new XYSeries("Vergence");
        xySeriesCollection.addSeries(xySerieMax);
        //xySerieMin = new XYSeries("Min");
        //xySeriesCollection.addSeries(xySerieMin);
        //Graphique
        JFreeChart chart = ChartFactory.createXYLineChart("", "", "", xySeriesCollection,
            PlotOrientation.VERTICAL, true, false, false);
        chartPanel = new ChartPanel( chart ) ;
        chartPanel.setBounds(470, 85, 400, 350);
        
        this.getContentPane().add (chartPanel) ;
        chartPanel.setVisible(true);
        
        chartPanel.setPopupMenu(null);
        
        
        //Layout du graphe
        chart.setBackgroundPaint(Color.CYAN);
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        //Axe zero
        Marker rangeMarker = new ValueMarker(0.0);
        rangeMarker.setPaint(Color.RED);
        rangeMarker.setStroke(new BasicStroke (2.0f) );
        plot.addRangeMarker(rangeMarker);
        
        //axe des Y
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        //axe des X
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        //domainAxis.setVisible(false) ;
        //Renderer
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesShapesVisible(1, true);
        //Rectangle rect = new Rectangle(3, 3);
        Ellipse2D circle = new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0);
        renderer.setSeriesShape(0, circle);
        plot.setRenderer(renderer);
        
        //On redessine
        //this.repaint();
        this.setAlwaysOnTop(false);
        //Default screen size
        int sc = jScreens.getSelectedIndex() ;
        DEFAULT_IMG_SIZE = (int) (screenDevices[sc].getDisplayMode().getHeight() * 0.80) ;
        if ( (DEFAULT_IMG_SIZE & 1) != 0 )  DEFAULT_IMG_SIZE--  ; // only odd values
        this.imgSize = DEFAULT_IMG_SIZE ;
    }
    
    public void addGraphMax (double max) {
        int n = xySerieMax.getItemCount() ;
        xySerieMax.add(n+1, max);
    }
    
    public void addGraphMin (double min) {
        int n = xySerieMin.getItemCount() ;
        xySerieMin.add(n+1, min);
    }
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel8 = new javax.swing.JLabel();
        jMenuItem1 = new javax.swing.JMenuItem();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jMax = new javax.swing.JSpinner();
        jTimeOut = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jMin = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jInitial = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        jWorkingDistance = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        jUnit = new javax.swing.JLabel();
        jUnit2 = new javax.swing.JLabel();
        jUnit3 = new javax.swing.JLabel();
        jUnit4 = new javax.swing.JLabel();
        jImg3D = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jStepC = new javax.swing.JSpinner();
        jUnit5 = new javax.swing.JLabel();
        jImgXBOX = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel9 = new javax.swing.JLabel();
        jUnit6 = new javax.swing.JLabel();
        jStepD = new javax.swing.JSpinner();
        jStart_Slide = new javax.swing.JButton();
        jSliderTimeOut = new javax.swing.JComboBox<>();
        jSeparator3 = new javax.swing.JSeparator();
        jStart_Img = new javax.swing.JButton();
        jScreensLabel = new javax.swing.JLabel();
        jImageChoice = new javax.swing.JComboBox<>();
        jScreens = new javax.swing.JComboBox<>();
        jRandomJumps = new javax.swing.JRadioButton();
        jVerticality = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        jUnit7 = new javax.swing.JLabel();
        jStart_Accommodation = new javax.swing.JButton();
        jSize_acc = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        jResetGraph = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jExperimental = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jDisparity = new javax.swing.JSpinner();
        jUnit8 = new javax.swing.JLabel();
        jImgXBOX1 = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jStims = new javax.swing.JComboBox<>();
        jLabel17 = new javax.swing.JLabel();
        jActivity = new javax.swing.JComboBox<>();
        j3DExperimental = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jConnection = new javax.swing.JMenuItem();
        jMenuCalibration = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jQuit = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuVibration = new javax.swing.JCheckBoxMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jMenuPlayers = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuSystemInfo = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenuHelp = new javax.swing.JMenuItem();

        jLabel8.setText("jLabel8");

        jMenuItem1.setText("jMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(java.awt.Color.cyan);
        setMinimumSize(new java.awt.Dimension(900, 700));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setText("Max :");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel2.setText("Time Out :");

        jMax.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jMax.setModel(new javax.swing.SpinnerNumberModel(10, 1, 60, 5));
        jMax.setEnabled(false);

        jTimeOut.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jTimeOut.setModel(new javax.swing.SpinnerNumberModel(20, 0, 120, 5));

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel3.setText("Min :");

        jMin.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jMin.setModel(new javax.swing.SpinnerNumberModel(-2, -30, 60, 1));
        jMin.setEnabled(false);

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel4.setText("Initial value :");

        jInitial.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jInitial.setModel(new javax.swing.SpinnerNumberModel(0, -30, 60, 1));
        jInitial.setEnabled(false);

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel6.setText("Distance :");

        jWorkingDistance.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jWorkingDistance.setModel(new javax.swing.SpinnerNumberModel(70, 20, 300, 10));
        jWorkingDistance.setEnabled(false);

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel7.setText("(cm)");

        jUnit.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jUnit.setText("\\u");

        jUnit2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jUnit2.setText("\\u");

        jUnit3.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jUnit3.setText("\\u");

        jUnit4.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jUnit4.setText("\\u");

        jImg3D.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Ressources/3d-RB.png"))); // NOI18N
        jImg3D.setText("image");
        jImg3D.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jImg3DMouseClicked(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel5.setText("Step (C) :");

        jStepC.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jStepC.setModel(new javax.swing.SpinnerNumberModel(2, 0, 30, 1));
        jStepC.setEnabled(false);

        jUnit5.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jUnit5.setText("seconds");

        jImgXBOX.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Ressources/xbox-icon64.png"))); // NOI18N

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel9.setText("Step (D) :");

        jUnit6.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jUnit6.setText("\\u");

        jStepD.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jStepD.setModel(new javax.swing.SpinnerNumberModel(0.5d, 0.25d, 5.0d, 0.25d));
        jStepD.setEnabled(false);

        jStart_Slide.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jStart_Slide.setText("Auto Slider");
        jStart_Slide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStart_SlideActionPerformed(evt);
            }
        });

        jSliderTimeOut.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jSliderTimeOut.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "slow", "medium", "fast", "very fast" }));
        jSliderTimeOut.setSelectedIndex(2);

        jStart_Img.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jStart_Img.setText("Image");
        jStart_Img.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStart_ImgActionPerformed(evt);
            }
        });

        jScreensLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jScreensLabel.setText("Screen :");

        jImageChoice.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jImageChoice.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "3d-practise", "3dring", "polytope", "art_png", "spi", "teseract", "poseidon" }));
        jImageChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jImageChoiceActionPerformed(evt);
            }
        });

        jScreens.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jScreens.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1" }));
        jScreens.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jScreensPropertyChange(evt);
            }
        });

        jRandomJumps.setBackground(java.awt.Color.cyan);
        jRandomJumps.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jRandomJumps.setText("Random Jumps");

        jVerticality.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jVerticality.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0.00", "0.25", "0.50", "0.75", "1.00", "1.25", "1.50" }));

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel10.setText("Verticality :");

        jUnit7.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jUnit7.setText("\\u");

        jStart_Accommodation.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jStart_Accommodation.setText("Accommodation");
        jStart_Accommodation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStart_AccommodationActionPerformed(evt);
            }
        });

        jSize_acc.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jSize_acc.setModel(new javax.swing.SpinnerNumberModel(9, 3, 12, 1));
        jSize_acc.setEnabled(false);

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel11.setText("Size :");

        jResetGraph.setBackground(java.awt.Color.orange);
        jResetGraph.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jResetGraph.setText("Reset Graph");
        jResetGraph.setToolTipText("Convergence");
        jResetGraph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jResetGraphActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        jLabel12.setText("Faire porter deux sphères opposées (comme -1/+2 ou +2/-3) selon les puissances désirées");

        jExperimental.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jExperimental.setText("Vergences");
        jExperimental.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jExperimentalActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel13.setText("Disparity :");

        jDisparity.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jDisparity.setModel(new javax.swing.SpinnerNumberModel(15, 5, 30, 1));

        jUnit8.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jUnit8.setText("pixels");

        jImgXBOX1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Ressources/keyboard.png"))); // NOI18N

        jLabel14.setText("Joueur 1 :");

        jLabel15.setText("Joueur 2 :");

        jLabel16.setText("Stims :");

        jStims.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Un carré", "Deux carrés", "Quatre ronds" }));
        jStims.setToolTipText("Number of stims");
        jStims.setEnabled(false);
        jStims.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStimsActionPerformed(evt);
            }
        });

        jLabel17.setText("Activity :");

        jActivity.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CD (prog.)", "C   (prog.)", "D   (prog.)", "CD (alterne)", "CD (jump)" }));
        jActivity.setToolTipText("Activiy game");
        jActivity.setEnabled(false);
        jActivity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jActivityActionPerformed(evt);
            }
        });

        j3DExperimental.setText("3D expérimental");
        j3DExperimental.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j3DExperimentalActionPerformed(evt);
            }
        });

        jMenu1.setText("Fichier");

        jConnection.setText("Connection serveur");
        jConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jConnectionActionPerformed(evt);
            }
        });
        jMenu1.add(jConnection);

        jMenuCalibration.setText("Calibration");
        jMenuCalibration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuCalibrationActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuCalibration);
        jMenu1.add(jSeparator4);

        jQuit.setText("Quitter");
        jQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jQuitActionPerformed(evt);
            }
        });
        jMenu1.add(jQuit);

        jMenuBar1.add(jMenu1);

        jMenu3.setText("Paramètres");

        jMenuVibration.setSelected(true);
        jMenuVibration.setText("xBox Vibration");
        jMenu3.add(jMenuVibration);
        jMenu3.add(jSeparator7);

        jMenuPlayers.setText("Joueurs");
        jMenuPlayers.setToolTipText("");
        jMenuPlayers.setEnabled(false);
        jMenu3.add(jMenuPlayers);

        jMenuBar1.add(jMenu3);

        jMenu2.setText("Aide");
        jMenu2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu2ActionPerformed(evt);
            }
        });

        jMenuSystemInfo.setText("Infos système");
        jMenuSystemInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuSystemInfoActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuSystemInfo);
        jMenu2.add(jSeparator5);

        jMenuHelp.setText("Instructions");
        jMenuHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuHelpActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuHelp);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(jScreensLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScreens, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jWorkingDistance, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jImgXBOX1)
                        .addGap(27, 27, 27)
                        .addComponent(jLabel15)
                        .addGap(18, 18, 18)
                        .addComponent(jImgXBOX, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(143, 143, 143)
                        .addComponent(jImg3D, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 431, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jResetGraph)
                .addGap(38, 38, 38))
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addGap(18, 18, 18)
                        .addComponent(jStims, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jExperimental, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel17)
                        .addGap(18, 18, 18)
                        .addComponent(jActivity, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(525, 525, 525))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSliderTimeOut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jStart_Slide, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jRandomJumps, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(256, 256, 256)
                        .addComponent(jImageChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jStart_Img, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(j3DExperimental)
                        .addGap(46, 46, 46))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator3)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 468, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel10))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jInitial, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jMin, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jStepC, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jVerticality, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jUnit2)
                                                    .addComponent(jUnit)))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(8, 8, 8)
                                                .addComponent(jUnit4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(30, 30, 30)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jLabel9))
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addGap(18, 18, 18)
                                                        .addComponent(jMax, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(jUnit3))
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addGap(17, 17, 17)
                                                        .addComponent(jStepD, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(jUnit6))))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(jTimeOut, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jUnit5, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(9, 9, 9)
                                        .addComponent(jUnit7, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(27, 27, 27)
                                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(16, 16, 16)
                                        .addComponent(jDisparity, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jUnit8, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(44, 44, 44)
                                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSize_acc, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(46, 46, 46)
                                .addComponent(jStart_Accommodation)
                                .addGap(26, 26, 26)
                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 528, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 8, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 431, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel14)
                                .addComponent(jLabel15))
                            .addComponent(jImgXBOX, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(36, 36, 36))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jImg3D, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jResetGraph)))
                            .addComponent(jImgXBOX1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jScreensLabel)
                    .addComponent(jScreens, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jWorkingDistance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel6))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jInitial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jUnit)
                    .addComponent(jLabel2)
                    .addComponent(jTimeOut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jUnit5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jUnit3)
                        .addComponent(jMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)
                        .addComponent(jUnit2))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel1)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jStepC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jUnit4)
                        .addComponent(jLabel9)
                        .addComponent(jUnit6)
                        .addComponent(jStepD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel5)))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jVerticality, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jUnit7)
                    .addComponent(jLabel13)
                    .addComponent(jDisparity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jUnit8))
                .addGap(18, 18, 18)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(jStims, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(jActivity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jStart_Slide)
                    .addComponent(jSliderTimeOut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRandomJumps)
                    .addComponent(jExperimental))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jImageChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jStart_Img)
                    .addComponent(j3DExperimental))
                .addGap(18, 18, 18)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jStart_Accommodation)
                    .addComponent(jSize_acc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12))
                .addGap(18, 28, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    
    private void jImg3DMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jImg3DMouseClicked
        if (OrthoStereogram.BR_glasses) {
            OrthoStereogram.BR_glasses = false;
            jImg3D.setIcon(new ImageIcon(imgRB));
        }
        else {
            OrthoStereogram.BR_glasses = true;
            jImg3D.setIcon(new ImageIcon(imgBR));
        }
    }//GEN-LAST:event_jImg3DMouseClicked

    private void jStart_SlideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStart_SlideActionPerformed
        //Check for verticality
        int hd = jVerticality.getSelectedIndex() ;
        //Create JFrame
        slide = new SlideStereogramView (jSliderTimeOut.getSelectedIndex(), (Integer) jMin.getValue(), (Integer) jMax.getValue(), (Integer) jWorkingDistance.getValue(), (Integer) jInitial.getValue(), hd, (Integer) jDisparity.getValue()) ;
        //Select screen to display
        int sc = jScreens.getSelectedIndex() ;
        /*if (graphicsEnv.getDefaultScreenDevice() != screenDevices[sc])*/
        screenDevices[sc].setFullScreenWindow(slide);
        //secondaryScreenDevice.setFullScreenWindow(slide);
        //Appearence
        //slide.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        
        //slide.setVisible(true);
        slide.repaint();
        //set after to avoid misplacement of panels
        slide.setAppearence (jRandomJumps.isSelected()) ;
        currentFrame = slide ; //Necessary for imgScale
    }//GEN-LAST:event_jStart_SlideActionPerformed

    private void jStart_ImgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStart_ImgActionPerformed
        
        //Check for verticality
        int hd = jVerticality.getSelectedIndex() ;
        
        String file = (String) jImageChoice.getSelectedItem() + ".png" ;
        image = new ImageStereogramView (file, jSliderTimeOut.getSelectedIndex(), (Integer) jMin.getValue(), (Integer) jMax.getValue(), (Integer) jWorkingDistance.getValue(), hd) ;
        //Select screen to display
        int sc = jScreens.getSelectedIndex() ;
        /*if (graphicsEnv.getDefaultScreenDevice() != screenDevices[sc])*/
        screenDevices[sc].setFullScreenWindow(image);
        //secondaryScreenDevice.setFullScreenWindow(image);
        //Appearence
        //image.setVisible(true); //image.repaint();
        image.setAppearence () ;
        currentFrame = image ; //Necessary for imgScale
    }//GEN-LAST:event_jStart_ImgActionPerformed

    private void jImageChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jImageChoiceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jImageChoiceActionPerformed

    private void jConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jConnectionActionPerformed
        ConnectDialog connect = new ConnectDialog (this, true) ;
        connect.setLocationRelativeTo(null);
        connect.setVisible (true) ;
        if (OrthoStereogram.user.nom != null) jConnection.setEnabled(false);
    }//GEN-LAST:event_jConnectionActionPerformed

    private void jQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jQuitActionPerformed
        OrthoStereogram.sortir();
    }//GEN-LAST:event_jQuitActionPerformed

    private void jMenu2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu2ActionPerformed
        
    }//GEN-LAST:event_jMenu2ActionPerformed

    private void jMenuHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuHelpActionPerformed
        //System.out.println ("help") ;
        HelpJDialog help = new HelpJDialog (this, true) ;
        help.setLocationRelativeTo(this);
        help.setVisible(true);
    }//GEN-LAST:event_jMenuHelpActionPerformed

    private void jMenuSystemInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSystemInfoActionPerformed
        SystemInfoJDialog info = new SystemInfoJDialog (this, true) ;
        
        //System infos
        info.jText.append("Java version : " + System.getProperty("java.version") + "\n" );
        info.jText.append("System is : " + System.getProperty("os.name") + " (" +  System.getProperty("os.version") + " " + System.getProperty("os.arch") + ")\n") ;
        info.jText.append("Free memory : " + Runtime.getRuntime().freeMemory() + "\n") ;
        
        //Computed resolution
        info.jText.append("Vergence : \n-----\n") ;
        info.jText.append("Estimated resolution : " + Toolkit.getDefaultToolkit().getScreenResolution() + " pixels/inch\n");
        if (isCalibrated) info.jText.append("Calibrated resolution: " + OrthoStereogram.screenResolution + " pixels/inch");
        else info.jText.append("No Calibration available") ;
        //Write datas
        info.jText.append("\n\nDetected screens: " + String.valueOf(numberOfScreens));
        for (int i=0; i<numberOfScreens; i++) {
            info.jText.append("\n-----\n");
            info.jText.append(String.valueOf(i) + ": ScreenName   : " + screenDevices[i].getIDstring() + "\n");
            info.jText.append(String.valueOf(i) + ": isFullScreen : " + screenDevices[i].isFullScreenSupported() + "\n");
            info.jText.append(String.valueOf(i) + ": resolution   : " + screenDevices[i].getDisplayMode().getWidth() + " x " + screenDevices[i].getDisplayMode().getHeight() + "\n");
            info.jText.append(String.valueOf(i) + ": refreshRate  : " + screenDevices[i].getDisplayMode().getRefreshRate() + "\n");
            if (graphicsEnv.getDefaultScreenDevice().equals(screenDevices[i]))
                info.jText.append(String.valueOf(i) + ": isPrimaryScreen " + "\n") ;
            //screenDevices[i].getDisplayMode().
        }
        info.jText.append("-----\n");
        
        //XBox infos
        info.jText.append("Manettes : " + controllers.getNumControllers());
        ControllerIndex currController = controllers.getControllerIndex(0);
        try {
            info.jText.append("\nManettes : " + currController.getName());
        } catch (ControllerUnpluggedException e) {}
        
        //Visible
        info.setLocationRelativeTo(this);
        info.setVisible(true);
    }//GEN-LAST:event_jMenuSystemInfoActionPerformed

    private void jStart_AccommodationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStart_AccommodationActionPerformed
        
        //Accommodation frame
        accommodation = new AccommodationJob ((Integer) jSize_acc.getValue()) ;

        //Select screen to display
        int sc = jScreens.getSelectedIndex() ;
        screenDevices[sc].setFullScreenWindow(accommodation);
        //On adapte l'apparence
        accommodation.setAppearence () ;
        currentFrame = accommodation ; //Necessary for imgScale
    }//GEN-LAST:event_jStart_AccommodationActionPerformed

    private void jResetGraphActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jResetGraphActionPerformed
        xySerieMax.clear();
    }//GEN-LAST:event_jResetGraphActionPerformed

    private void jScreensPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jScreensPropertyChange
        //Default screen size
        int sc = jScreens.getSelectedIndex() ;
        if (screenDevices == null) return ;
        DEFAULT_IMG_SIZE = (int) (screenDevices[sc].getDisplayMode().getHeight() * 0.80) ;
        if ( (DEFAULT_IMG_SIZE & 1) != 0 )  DEFAULT_IMG_SIZE--  ; // only odd values
        this.imgSize = DEFAULT_IMG_SIZE ;
    }//GEN-LAST:event_jScreensPropertyChange

    private void jExperimentalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jExperimentalActionPerformed
        //Check for verticality
        int hd = jVerticality.getSelectedIndex() ;
        //Select screen to display
        int sc = jScreens.getSelectedIndex() ;
        //On ajuste la taille du stéréogramme si besoin
        if (screenDevices[sc].getDisplayMode().getHeight() < this.imgSize) this.imgSize = DEFAULT_IMG_SIZE ;
        //On ajuste la taille du stéréogramme si besoin
        if (screenDevices[sc].getDisplayMode().getHeight() < this.imgSize) this.imgSize = DEFAULT_IMG_SIZE ;
        //La fenetre
        doubleSt = new DoubleStereogram (imgSize, (Integer) jWorkingDistance.getValue(), (Integer) jInitial.getValue(), hd, (int) jStepC.getValue(), (double) jStepD.getValue()) ;
        
        //On affiche
        this.setState(JFrame.ICONIFIED);
        screenDevices[sc].setFullScreenWindow(doubleSt);
        doubleSt.setAppearence(jStims.getSelectedIndex(), (Integer) jMax.getValue(), (Integer) jMin.getValue(), (Integer) jTimeOut.getValue(), jActivity.getSelectedIndex(), (Integer) jDisparity.getValue());
        currentFrame = doubleSt ; //Necessary for imgScale
    }//GEN-LAST:event_jExperimentalActionPerformed

    private void jMenuCalibrationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuCalibrationActionPerformed
        Calibrate cal = new Calibrate (this, true) ;
        cal.setLocationRelativeTo(this);
        cal.setVisible(true);
    }//GEN-LAST:event_jMenuCalibrationActionPerformed

    private void jStimsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStimsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jStimsActionPerformed

    private void jActivityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jActivityActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jActivityActionPerformed

    private void j3DExperimentalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j3DExperimentalActionPerformed
       // This method is invoked on the EDT thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                initAndShowGUI();
            }
        });
    }//GEN-LAST:event_j3DExperimentalActionPerformed

    
    private static void initAndShowGUI() {
        // This method is invoked on the EDT thread
        JFrame frame = new JFrame("Swing and JavaFX");
        final JFXPanel fxPanel = new JFXPanel();
        frame.add(fxPanel);
        frame.setSize(500, 500);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
            }
       });
    }

    private static void initFX(JFXPanel fxPanel) {
        // This method is invoked on the JavaFX thread
        Scene scene = createScene();
        fxPanel.setScene(scene);
    }

    private static Scene createScene() {
        Group  root  =  new  Group();
        Scene  scene  =  new  Scene(root, javafx.scene.paint.Color.ALICEBLUE);
        Text  text  =  new  Text();
        
        text.setX(40);
        text.setY(100);
        text.setFont(new Font(25));
        text.setText("Welcome JavaFX!");

        root.getChildren().add(text);
        
        PhongMaterial phongMaterial = new PhongMaterial();
        phongMaterial.setDiffuseColor(javafx.scene.paint.Color.DARKRED);
        phongMaterial.setSpecularColor(javafx.scene.paint.Color.RED);
        
        //Creating an object of the class Box 
        Box box = new Box();
        box.setLayoutX(150);
        box.setLayoutY(250);
        box.setWidth(100.0); 
        box.setHeight(100.0);   
        box.setDepth(100.0);
        box.setMaterial(phongMaterial);
        
        Rotate rxBox = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
        Rotate ryBox = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
        Rotate rzBox = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);
        rxBox.setAngle(30);
        ryBox.setAngle(50);
        rzBox.setAngle(30);
        box.getTransforms().addAll(rxBox, ryBox, rzBox);
        
        root.getChildren().add(box);
        return (scene);
    }
    
    static public boolean imgScale (double factor) {
        int tmp = (int) (imgSize * factor) ;
        //Only odd sizes
        if ( (tmp & 1) != 0 )  tmp--  ;
        //if too big or too small..
        if (tmp > currentFrame.getContentPane().getHeight() | tmp < 200) return false ;
        else {
            imgSize = tmp ;
            return true ;
        }
    }
    
    //Mise à jour des limites une fois connecté
    public void connected () {
        
        //Titre
        setTitle ("stereoVergences ("+OrthoStereogram.VERSION+") - Connecté au serveur : " + OrthoStereogram.user.titre + " " +OrthoStereogram.user.nom.toUpperCase() + " " + OrthoStereogram.user.prenom ) ;
        //Nouvelles valeurs de base
        jInitial.setEnabled(true);
        jMin.setValue(new Integer(-4)); jMin.setEnabled(true);
        jMax.setValue(new Integer(20)); jMax.setEnabled(true);
        jStepC.setEnabled(true); jStepD.setEnabled(true);
        jWorkingDistance.setEnabled(true);
        jConnection.setEnabled(false);
        jMenuCalibration.setEnabled(true) ;
        jStims.setEnabled(true);
        jActivity.setEnabled(true);
        jSize_acc.setEnabled(true);
        //On regarde s'il existe une calibration
        if (OrthoStereogram.mySQLConnection.getCalibration()) {
            //jCalibrate.setBackground(Color.GREEN.brighter());
            isCalibrated = true ;
        } 
        OrthoStereogram.mySQLConnection.getAnaglyphes();
        jImg3DMouseClicked(null);
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton j3DExperimental;
    private javax.swing.JComboBox<String> jActivity;
    public static javax.swing.JMenuItem jConnection;
    private javax.swing.JSpinner jDisparity;
    private javax.swing.JButton jExperimental;
    private javax.swing.JComboBox<String> jImageChoice;
    private javax.swing.JLabel jImg3D;
    public static javax.swing.JLabel jImgXBOX;
    public static javax.swing.JLabel jImgXBOX1;
    private javax.swing.JSpinner jInitial;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSpinner jMax;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuCalibration;
    private javax.swing.JMenuItem jMenuHelp;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuPlayers;
    private javax.swing.JMenuItem jMenuSystemInfo;
    public static javax.swing.JCheckBoxMenuItem jMenuVibration;
    private javax.swing.JSpinner jMin;
    private javax.swing.JMenuItem jQuit;
    public static javax.swing.JRadioButton jRandomJumps;
    private javax.swing.JButton jResetGraph;
    private javax.swing.JComboBox<String> jScreens;
    private javax.swing.JLabel jScreensLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JSpinner jSize_acc;
    private javax.swing.JComboBox<String> jSliderTimeOut;
    private javax.swing.JButton jStart_Accommodation;
    private javax.swing.JButton jStart_Img;
    private javax.swing.JButton jStart_Slide;
    private javax.swing.JSpinner jStepC;
    private javax.swing.JSpinner jStepD;
    private javax.swing.JComboBox<String> jStims;
    private javax.swing.JSpinner jTimeOut;
    private javax.swing.JLabel jUnit;
    private javax.swing.JLabel jUnit2;
    private javax.swing.JLabel jUnit3;
    private javax.swing.JLabel jUnit4;
    private javax.swing.JLabel jUnit5;
    private javax.swing.JLabel jUnit6;
    private javax.swing.JLabel jUnit7;
    private javax.swing.JLabel jUnit8;
    private javax.swing.JComboBox<String> jVerticality;
    private javax.swing.JSpinner jWorkingDistance;
    // End of variables declaration//GEN-END:variables

    


    @Override
    public void windowOpened(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowClosing(WindowEvent e) {
        controllers.quitSDLGamepad();
        OrthoStereogram.sortir () ;
    }

    @Override
    public void windowClosed(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowIconified(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowActivated(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

