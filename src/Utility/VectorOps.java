package Utility;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

public class VectorOps 
{	
	public static Matrix3d calculateRotationMatrix(Vector3d axis, double angle)
	{		
		double cosAng = Math.cos(angle);
		double sinAng = Math.sin(angle);		
		double[] angMat = 	{
								cosAng+axis.x*axis.x*(1-cosAng), axis.x*axis.y*(1-cosAng)-axis.z*sinAng, axis.x*axis.z*(1-cosAng)+axis.y*sinAng,
								axis.y*axis.x*(1-cosAng)+axis.z*sinAng, cosAng+axis.y*axis.y*(1-cosAng), axis.y*axis.z*(1-cosAng)-axis.x*sinAng,
								axis.z*axis.x*(1-cosAng)-axis.y*sinAng, axis.z*axis.y*(1-cosAng)+axis.x*sinAng, cosAng+axis.z*axis.z*(1-cosAng)								
							};
	
		Matrix3d rotMat = new Matrix3d(angMat);
		return rotMat;
	}
	
	public static Vector3d applyRotTransform(Matrix3d rot, Vector3d vec)
	{
		Vector3d row1 = new Vector3d();
		Vector3d row2 = new Vector3d();
		Vector3d row3 = new Vector3d();
		
		rot.getRow(0, row1);
		rot.getRow(1, row2);
		rot.getRow(2, row3);
		
		Vector3d result = new Vector3d();
		result.x = row1.dot(vec);
		result.y = row2.dot(vec);
		result.z = row3.dot(vec);
		
		return result;
	}
}