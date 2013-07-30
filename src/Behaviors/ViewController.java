package Behaviors;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;

import javax.media.j3d.Canvas3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import Utility.VectorOps;


enum KeyVal
{
	W(0),A(1),S(2),D(3),Q(4),E(5),C(6),V(7),CTL(8),SHT(9), NaN(-1);
	
	private KeyVal(int c) 
	{
		code = c;
	}
		 
	public int getCode() 
	{
		return code;
	}
	public static int numCodes()
	{
		return values().length;
	}
	private int code;

}

public class ViewController extends PhysicsBehavior implements MouseMotionListener, KeyListener
{

	public ViewController(TransformGroup tg, Canvas3D display) 
	{
		super(tg);
		
		mouseLoc      = new Vector3d();
		
		appliedForce  = new Vector3d();
		appliedTorque = new Vector3d();
		
		this.display = display;
		display.addMouseMotionListener(this);
		display.addKeyListener(this);
		
		keysPressed = new int[KeyVal.numCodes()];
		
		forwardForce  = 0.1;
		backwardForce = 0.1;
		leftForce     = 0.1;
		rightForce    = 0.1;
		upForce       = 0.1;
		downForce     = 0.1;
		rollForce = 0.001;
		maxMouseTorque = 0.001;
		maxMouseDistance = 0.0;
		mouseDeadZone    = 50.0;
		uniformForce  = 0.1;
		
		throttle = 0.5;
		
		mode = DIRECTIONAL_FORCE;
	}
	
	@Override
	public void processStimulus(Enumeration criteria)
	{
		super.processStimulus(criteria);
		
		_processInput();
	}
	
	
	@Override
	public void keyPressed(KeyEvent e) 
	{
		KeyVal key = _keyHashFunction(e.getKeyCode());
		if(key != KeyVal.NaN)
		{
			keysPressed[key.getCode()] = 1;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) 
	{		
		KeyVal key = _keyHashFunction(e.getKeyCode());
		if (key != KeyVal.NaN) 
		{
			keysPressed[key.getCode()] = 0;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) 
	{
		
	}

	@Override
	public void mouseDragged(MouseEvent e) 
	{		
		Vector3d origin = new Vector3d(display.getWidth()/2, display.getHeight()/2 * -1, 0);
		if(UPDATE_ON_DRAG) 
		{
			mouseLoc.x = e.getX();
			mouseLoc.y = e.getY() * -1;
			mouseLoc.sub(origin);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{		
		Vector3d origin = new Vector3d(display.getWidth()/2, display.getHeight()/2*-1, 0);
		if(UPDATE_ON_MOVE) 
		{
			mouseLoc.x = e.getX();
			mouseLoc.y = e.getY() * -1;
			mouseLoc.sub(origin);
		} 
		else {
			mouseLoc.x = 0;
			mouseLoc.y = 0;
		}
		maxMouseDistance = Math.pow(Math.pow(display.getWidth()/2, 2)+Math.pow(display.getHeight()/2, 2), 0.5);
	}
	
	private void _processInput()
	{
		Transform3D tf = new Transform3D();
		tg.getTransform(tf);
		
		Matrix3d rotMat = new Matrix3d();
		tf.getRotationScale(rotMat);
		
		
		Vector3d rollAxis  = VectorOps.applyRotTransform(rotMat, initRollAxis);
		Vector3d pitchAxis = VectorOps.applyRotTransform(rotMat, initPitchAxis);
		Vector3d yawAxis   = VectorOps.applyRotTransform(rotMat, initYawAxis);

		rollAxis.normalize();
		pitchAxis.normalize();
		yawAxis.normalize();
		
		Vector3d force = new Vector3d();
		Vector3d torque = new Vector3d();
		
		
		Vector3d mouseRotTorque = VectorOps.applyRotTransform(rotMat, mouseLoc);
		mouseRotTorque.scale(maxMouseTorque/maxMouseDistance);
		
		
		
		if(keysPressed[KeyVal.W.getCode()] == 1)
		{
			Vector3d forward = (Vector3d)rollAxis.clone();
			forward.scale(forwardForce);
			force.add(forward);
		}
		if(keysPressed[KeyVal.A.getCode()] == 1)
		{
			Vector3d left = (Vector3d)pitchAxis.clone();
			left.negate();
			left.scale(leftForce);
			force.add(left);
		}
		if(keysPressed[KeyVal.S.getCode()] == 1)
		{
			Vector3d backward = (Vector3d)rollAxis.clone();
			backward.negate();
			backward.scale(backwardForce);
			force.add(backward);
		}
		if(keysPressed[KeyVal.D.getCode()] == 1)
		{
			Vector3d right = (Vector3d)pitchAxis.clone();
			right.scale(rightForce);
			force.add(right);
		}
		if(keysPressed[KeyVal.Q.getCode()] == 1)
		{
			Vector3d rollLeft = (Vector3d)rollAxis.clone();
			rollLeft.scale(rollForce);
			torque.add(rollLeft);
		}
		if(keysPressed[KeyVal.E.getCode()] == 1)
		{
			Vector3d rollRight = (Vector3d)rollAxis.clone();
			rollRight.negate();
			rollRight.scale(rollForce);
			torque.add(rollRight);
		}
		if(keysPressed[KeyVal.C.getCode()] == 1)
		{
			Vector3d up = (Vector3d)yawAxis.clone();
			up.scale(upForce);
			force.add(up);
		}
		if(keysPressed[KeyVal.V.getCode()] == 1)
		{
			Vector3d down = (Vector3d)yawAxis.clone();
			down.negate();
			down.scale(downForce);
			force.add(down);
		}
		if(keysPressed[KeyVal.CTL.getCode()] == 1 && throttle > 0)
		{
			throttle -= 0.001;
		}
		if(keysPressed[KeyVal.SHT.getCode()] == 1 && throttle < 1)
		{
			throttle += 0.001;
		}
		if(mouseLoc.length() > mouseDeadZone)
		{
			Vector3d tmpVect = new Vector3d();
			tmpVect.cross(mouseRotTorque, rollAxis);
			torque.add(tmpVect);	
		}
		
		force.scale(throttle);
		// stop applying force in previous direction
		appliedForce.negate();
		this.applyForce(appliedForce); 
		appliedTorque.negate();
		this.applyTorque(appliedTorque);
		
		if(mode == UNIFORM_FORCE && force.length() != 0)
		{
			force.normalize();
			force.scale(uniformForce);
		}
		
		this.applyForce(force);
		this.applyTorque(torque);
		appliedForce  = force;
		appliedTorque = torque;
	}
	
	private KeyVal _keyHashFunction(int key)
	{
		switch(key)
		{
		case 87: // W
			return KeyVal.W;
		case 65: // A
			return KeyVal.A;
		case 83: // S
			return KeyVal.S;
		case 68: // D
			return KeyVal.D;
		case 81: // Q
			return KeyVal.Q;
		case 69: // E
			return KeyVal.E;
		case 67: // C
			return KeyVal.C;
		case 86: // V
			return KeyVal.V;
		case 16: // shift
			return KeyVal.SHT;
		case 17:
			return KeyVal.CTL;
		}
		return KeyVal.NaN;
	}
	
	public double getForwardForce()
	{
		return forwardForce;
	}
	public double getBacwardForce()
	{
		return backwardForce;
	}
	public double getLeftForce()
	{
		return leftForce;
	}
	public double getRightForce()
	{
		return rightForce;
	}
	public double getUpForce()
	{
		return upForce;
	}
	public double getRollForce()
	{
		return rollForce;
	}
	public double getMaxMouseTorque()
	{
		return maxMouseTorque;
	}
	public double getMouseDeadZone()
	{
		return mouseDeadZone;
	}
	public void setForwardForce(double force)
	{
		forwardForce = force;
	}
	public void setBackwardForce(double force)
	{
		backwardForce = force;
	}
	public void setLeftForce(double force)
	{
		leftForce = force;
	}
	public void setRightForce(double force)
	{
		rightForce = force;
	}
	public void setUpForce(double force)
	{
		upForce = force;
	}
	public void setDownForce(double force)
	{
		downForce = force;
	}
	public void setRollForce(double force)
	{
		rollForce = force;
	}
	public void setMaxMouseTorque(double torque)
	{
		maxMouseTorque = torque;
	}
	public void setUniformForce(double force)
	{
		uniformForce = force;
	}
	public void setMode(int mode)
	{
		this.mode = mode;
	}
	
	Canvas3D display;

	int[] keysPressed;

	boolean UPDATE_ON_DRAG = true;
	boolean UPDATE_ON_MOVE = true;
	
	Vector3d mouseLoc;
	
	Vector3d appliedForce;
	Vector3d appliedTorque;
	
	double forwardForce;
	double backwardForce;
	double leftForce;
	double rightForce;
	double upForce;
	double downForce;
	double rollForce;
	double maxMouseTorque;
	double uniformForce;
	
	double maxMouseDistance;
	double mouseDeadZone;
	
	double throttle;
	
	static Vector3d initRollAxis  = new Vector3d(0,0,-1.0);
	static Vector3d initPitchAxis = new Vector3d(1.0,0,0);
	static Vector3d initYawAxis   = new Vector3d(0,1.0,0);
	
	int mode;
	static int UNIFORM_FORCE = 0;
	static int DIRECTIONAL_FORCE = 1;
}
