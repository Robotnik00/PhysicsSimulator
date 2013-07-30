package Behaviors;

import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnCollisionEntry;
import javax.media.j3d.WakeupOnElapsedTime;
import javax.media.j3d.WakeupOr;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import Utility.VectorOps;



public class PhysicsBehavior extends Behavior
{
	// default constructor
	public PhysicsBehavior(TransformGroup tg) 
	{
		this.tg = tg;
		
		translationVelocity = new Vector3d();
		rotationVelocity = new Vector3d(0,0,0);
		
		force  = new Vector3d();
		torque = new Vector3d();
		
		friction           = 0.9;
		airFriction        = 0.5;
		rotationalFriction = 0.99;
		dragForce = new Vector3d();
		
		collisionBounds = new BoundingSphere();
		collisionBounds.setRadius(1.0);
		setBounds(collisionBounds);
		
		mass = 1.0;
		momentOfInertia = 10.0;
	}
	
	@Override
	public void initialize() 
	{
		WakeupCriterion[] criterias = {new WakeupOnElapsedTime(10), new WakeupOnCollisionEntry(collisionBounds)};
		WakeupOr wakeupon= new WakeupOr(criterias);
		wakeupOn(wakeupon); 
	}

	@Override
	public void processStimulus(Enumeration criteria) 
	{
		WakeupCriterion wakeup;
		while(criteria.hasMoreElements())
		{
			wakeup = (WakeupCriterion)criteria.nextElement();
			if(wakeup instanceof WakeupOnElapsedTime)
			{
				_updateTransform();
				
			}
			else if(wakeup instanceof WakeupOnCollisionEntry)
			{
				WakeupOnCollisionEntry collision = (WakeupOnCollisionEntry)wakeup;
				_processCollision(collision);
			}
		}
		
		WakeupOnCollisionEntry onCollision = new WakeupOnCollisionEntry(collisionBounds);
	
		WakeupCriterion[] criterias = {new WakeupOnElapsedTime(10), new WakeupOnCollisionEntry(collisionBounds)};
		
		WakeupOr wakeupon= new WakeupOr(criterias);
		wakeupOn(wakeupon); 
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	/// Updates the location of the object every frame
	//////////////////////////////////////////////////////////////////////////////////////////
	protected synchronized void _updateTransform()
	{

		Transform3D tf = new Transform3D();
		tg.getTransform(tf);
		
		Vector3d loc = new Vector3d();
		tf.get(loc);
		Matrix3d orientation = new Matrix3d();
		tf.getRotationScale(orientation);

		
		loc.add(translationVelocity);

		
		Transform3D newTransform = new Transform3D();
		Matrix3d rotationTransform = VectorOps.calculateRotationMatrix(rotationVelocity, rotationVelocity.length()-1);
		rotationTransform.mul(orientation);
		rotationTransform.normalize();
		newTransform.setRotation(rotationTransform);
		
		
		newTransform.setTranslation(loc);
		
		tg.setTransform(newTransform);
		
		
		//translationVelocity.scale(airFriction);
		rotationVelocity.scale(rotationalFriction);
	
		if(translationVelocity.length() != 0)
		{
			this.dragForce.negate();
			force.add(this.dragForce);
			Vector3d dragForce = (Vector3d) translationVelocity.clone();
			dragForce.normalize();
			dragForce.scale(translationVelocity.lengthSquared() * airFriction);
			dragForce.negate();
			force.add(dragForce);
			this.dragForce = dragForce;
		}
		Vector3d acceleration = (Vector3d)force.clone();
		acceleration.scale(1/mass);
		translationVelocity.add(acceleration);
		
		Vector3d rotAcceleration = (Vector3d)torque.clone();
		rotAcceleration.scale(1/momentOfInertia);
		rotationVelocity.add(rotAcceleration);
		if(debug)
		{
			System.out.printf("velocity:\n  translation: %-20s\n  rotation:    %-20s\n", translationVelocity, rotationVelocity);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	/// Calculates the new velocities in the event of a collision
	//////////////////////////////////////////////////////////////////////////////////////////
	protected synchronized void _processCollision(WakeupOnCollisionEntry collision)
	{
		//if(debug)
		{
			System.out.printf("%s\n", collision.getTriggeringPath().getObject());
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	/// Accessors:
	///////////////////////////////////////////////////////////////////////////////////////////
	public synchronized void setTranslationVelocity(Vector3d velocity)
	{
		translationVelocity = velocity;
	}
	public synchronized void setRotationalVelocity(Vector3d rot)
	{
		rotationVelocity = rot;
	}
	public synchronized void setFriction(double friction)
	{
		this.friction = friction;
	}
	public synchronized void setAirFriction(double friction)
	{
		airFriction = friction;
	}
	public synchronized void setRotationalFriction(double friction)
	{
		rotationalFriction = friction;
	}
	public synchronized void setMass(double mass)
	{
		this.mass = mass;
	}
	public synchronized void setTranformGroup(TransformGroup tg)
	{
		this.tg = tg;
	}
	public synchronized Vector3d getTranslationalVelocity()
	{
		return translationVelocity;
	}
	public synchronized Vector3d getRotationVelocity()
	{
		return rotationVelocity;
	}
	public synchronized double getFriction()
	{
		return friction;
	}
	public synchronized double getAirFriction()
	{
		return airFriction;
	}
	public synchronized double getRotationalFriction()
	{
		return rotationalFriction;
	}
	public synchronized double getMass()
	{
		return mass;
	}
	public synchronized TransformGroup getTransformGroup()
	{
		return tg;
	}
	public synchronized void setForce(Vector3d force)
	{
		this.force = force;
	}
	public synchronized void applyForce(Vector3d force)
	{
		this.force.add(force);
	}
	public synchronized void stopAllForces()
	{
		this.force = new Vector3d();
	}
	public synchronized void setTorque(Vector3d torque)
	{
		this.torque = torque;
	}
	public synchronized void applyTorque(Vector3d torque)
	{
		this.torque.add(torque);
	}
	public synchronized void stopAllTorque()
	{
		this.torque = new Vector3d();
	}
	public synchronized void setDebugMode(boolean debug)
	{
		this.debug = debug;
	}
	
	protected Vector3d translationVelocity;
	protected Vector3d rotationVelocity;
	
	protected Vector3d dragForce;
	
	protected Vector3d force;
	protected Vector3d torque;

	
	
	protected double mass;
	protected double momentOfInertia;
	
	protected double friction;
	protected double airFriction;
	protected double rotationalFriction;

	protected TransformGroup tg;
	
	protected BoundingSphere collisionBounds;
	
	
	boolean debug = false;
}
