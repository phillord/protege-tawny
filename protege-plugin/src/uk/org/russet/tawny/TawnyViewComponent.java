package uk.org.russet.tawny;

import clojure.lang.RT;
import clojure.lang.Var;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.hierarchy.AssertedClassHierarchyProvider;
import org.protege.editor.owl.ui.tree.OWLModelManagerTree;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;


public class TawnyViewComponent extends AbstractOWLViewComponent {
    private static final long serialVersionUID = -4515710047558710080L;
    private static final Logger log = Logger.getLogger(TawnyViewComponent.class);

    // this is all crap -- the class doesn't load -- not to thread the initialization...

    @Override
    protected void initialiseOWLView() throws Exception {
        // System.out.println("this classLoader");
        // for(Object url: ((java.net.URLClassLoader)
        //                  (this.getClass().getClassLoader())).getURLs()){
        //     System.out.println(url);
        // }
        final Runnable b4 = new Runnable(){
                public void run(){
                    setLayout(new BorderLayout());
                    add(new JLabel("Initializing Tawny"), BorderLayout.CENTER);
                    validate();
                    repaint();
                }
            };

        final Runnable during = new Runnable(){
                public void run(){
                    System.out.println( "Attempting to make console");
                    Thread.currentThread()
                        .setContextClassLoader(this.getClass().getClassLoader());
                    // Load the main repl library and instantiate the runtime
                    // as a side effect. This bit takes ages, and I don't know
                    // how to speed it up.
                    try{
                        RT.loadResourceScript("tawny/protege/repl.clj");
                    }
                    catch(java.io.IOException exp){
                        throw new RuntimeException(exp);
                    }
                }
            };

        final Runnable after = new Runnable(){
                public void run(){
                    Var foo = RT.var("tawny.protege.repl", "new-console");
                    JComponent console = (JComponent)foo.invoke(getOWLModelManager());
                    // and finally instantiate the GUI
                    removeAll();
                    setLayout(new BorderLayout());
                    add(console, BorderLayout.CENTER);
                    log.info("Tawny View Component initialized");
                    validate();
                    repaint();
                }
            };

        Runnable control = new Runnable(){
                public void run(){
                    try{
                        // in paint thread
                        SwingUtilities.invokeAndWait( b4 );
                        // out of pain thread
                        during.run();
                        // in paint thread
                        SwingUtilities.invokeAndWait( after );
                    }
                    catch(InterruptedException exp){
                        throw new RuntimeException(exp);
                    }
                    catch(java.lang.reflect.InvocationTargetException exp){
                        throw new RuntimeException(exp);
                    }
                }
            };
        new Thread(control).start();

    }

    @Override
    protected void disposeOWLView() {

    }

}
