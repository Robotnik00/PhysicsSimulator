import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.media.j3d.Appearance;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingLeaf;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Locale;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.ViewPlatform;
import javax.media.j3d.VirtualUniverse;
import javax.swing.JFrame;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import Behaviors.PhysicsBehavior;
import Behaviors.ViewController;

import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;


public class MainWindow
{
	public static void main(String[] args)
	{
		new MainWindow();
	}
	public MainWindow()
	{
		_buildGUI();
		_buildUniverse();
		_buildViewer();
	}
	private void _buildGUI()
	{
		window = new JFrame("Physics Simulator");
		window.setSize(500, 500);
		window.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		
		viewingCanvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
		
		window.add(viewingCanvas, c);
		
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
	}
	private void _buildUniverse()
	{
		vUniverse = new VirtualUniverse();
		universeLocale = new Locale(vUniverse);
		BranchGroup staticObjs = new BranchGroup();
		addSphere(staticObjs, new Vector3f(0,0,-15.0f), 3.0f, new Color3f(0,1,0));
		addLight(staticObjs, new Vector3f(-0.5f,-0.5f,-1.0f), new Vector3f(), new Color3f(1,1,1));
		universeLocale.addBranchGraph(staticObjs);
	}
	private void _buildViewer()
	{
		viewBG = new BranchGroup();
		
		viewTG = new TransformGroup();
		
		viewer = new ViewPlatform();
		
		view   = new View();
		PhysicalBody viewBody = new PhysicalBody();
		PhysicalEnvironment viewEnv = new PhysicalEnvironment();
		view.setPhysicalBody(viewBody);
		view.setPhysicalEnvironment(viewEnv);
		view.addCanvas3D(viewingCanvas);
		view.setBackClipDistance(10000.0f);
		view.attachViewPlatform(viewer);
		
		viewTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		viewTG.addChild(viewer);
		viewBG.addChild(viewTG);
		
		ViewController b = new ViewController(viewTG, viewingCanvas);
		BoundingSphere bounds = new BoundingSphere();
		bounds.setRadius(1);
		b.clearCapability(0);
		b.setCapability(Behavior.ENABLE_COLLISION_REPORTING);
		b.setCapability(Behavior.ALLOW_COLLIDABLE_READ);
		b.setCapability(Behavior.ALLOW_COLLIDABLE_WRITE);
		b.setCapability(Behavior.ALLOW_BOUNDS_WRITE);
		b.setCapability(Behavior.ALLOW_BOUNDS_READ);
		b.setSchedulingBounds(bounds);
		b.setCollidable(true);
		b.setEnable(true);
	
		viewTG.addChild(b);
		viewTG.setCollidable(true);
		viewBG.setCollidable(true);
		
		//b.setDebugMode(true);
		
		//b.setTranslationVelocity(new Vector3d(0,0,0.6));
		//b.setRotationalVelocity(new Vector3d(-0.01,0,0));
		//b.applyForce(new Vector3d(0,0,-.01));

		universeLocale.addBranchGraph(viewBG);
		
	}	
	public void addLight(BranchGroup bg, Vector3f dir, Vector3f loc, Color3f c)
	{
		DirectionalLight light = new DirectionalLight(c, dir);
		
		BoundingSphere bound = new BoundingSphere(new Point3d(loc), 10000.0);
		light.setInfluencingBounds(bound);
		
		TransformGroup lightTG = new TransformGroup();
		Transform3D lightTF = new Transform3D();
		lightTF.setTranslation(loc);
		lightTG.addChild(light);
		bg.addChild(lightTG);
	}
	public void addSphere(BranchGroup bg, Vector3f loc, float radius, Color3f c)
	{
		Sphere sphere = new Sphere(radius, Primitive.GENERATE_NORMALS, 50);
		
		Appearance ap = new Appearance();
		Material mat = new Material();
		mat.setDiffuseColor(c);
		mat.setSpecularColor(new Color3f(1, 1, 1));
		//mat.setShininess(1);
		mat.setAmbientColor(c);
		ap.setMaterial(mat);
		RenderingAttributes ra = new RenderingAttributes();
		ra.setStencilEnable(true);
		ap.setRenderingAttributes(ra);
		sphere.setAppearance(ap);
		TransformGroup sphereTG = new TransformGroup();
		sphereTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	
		sphereTG.addChild(sphere);
		Transform3D sphereTF = new Transform3D();
		sphereTF.setTranslation(loc);
		sphereTG.setTransform(sphereTF);
		BoundingSphere bounds = new BoundingSphere();
		bounds.setRadius(1.0);
		sphereTG.setCollidable(true);
		sphereTG.setCollisionBounds(bounds);
		PhysicsBehavior b = new PhysicsBehavior(sphereTG);
		b.setSchedulingBounds(new BoundingSphere());
		sphereTG.addChild(b);
		bg.addChild(sphereTG);
		
		
	}
	
	
	
	JFrame window;
	
	Canvas3D viewingCanvas;
	
	VirtualUniverse vUniverse;
	Locale universeLocale;
	
	BranchGroup viewBG;
	ViewPlatform viewer;
	View view;
	TransformGroup viewTG;
	
	
}
