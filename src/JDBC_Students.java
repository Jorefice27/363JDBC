import java.sql.*;

//student 118784412 should be a sophomore with 45 credits with a gpa of 2.9128889
public class JDBC_Students {
	
	public static void main(String[] args)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch(Exception e)
		{
			System.out.println("Could not connect to driver");
		}
		
		try
		{
			//Connect to database
			String dbUrl = "jdbc:mysql://csdb.cs.iastate.edu:3306/db363jorefice";
			String user = "dbu363jorefice";
			String password = "EulL2511";
			Connection conn = DriverManager.getConnection(dbUrl, user, password);
			System.out.println("Connected");
			
			
			// update student records
			// compute new GPAs and classifications (all classes are 3 credits)
			
			
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("select s.StudentID, Classification, GPA, CreditHours, Grade" + " "
					+ "from Student s inner join Enrollment e" + " "
					+ "on s.studentID = e.StudentID" + " "
					+ "order by StudentID");
			
//			select s.StudentID, Classification, GPA, CreditHours, Grade
//			from Student s inner join Enrollment e
//			on s.studentID = e.studentID
//			order by StudentID;

			
			//StudentID, Classification, GPA, CreditHours, Grades
			int id = 0;
			int credits = 0;
			double gpa = 0.0;
			String classification = "";
			int semesterCredits = 0;
			int semesterGP = 0;
			PreparedStatement pstatement = conn.prepareStatement("Update Student" + " " 
					+ "set CreditHours=?, GPA=?, Classification=?" + " "
					+ "where StudentID=?");
			boolean first = true;
			while(rs.next())
			{
				int newid = rs.getInt("StudentID");
				// count the GPA and number of credits for ALL classes the student is taking
				if(newid == id)
				{
					semesterGP += getGradeValue(rs.getString("Grade"));
					semesterCredits += 3;
				}
				else
				{
					if(!first)
					{
						// calculate new values
						gpa = ((gpa * credits) + semesterGP) / (credits + semesterCredits);
						credits += semesterCredits;
						classification = getClassification(credits);
						// update the last student
						pstatement.setInt(1, credits);
						pstatement.setDouble(2, gpa);
						pstatement.setString(3, classification);
						pstatement.setInt(4, id);
						pstatement.executeUpdate();
					}
					else
					{
						first = false;
					}
					
					// get information about the new student
					id = newid;
					credits = rs.getInt("CreditHours");
					gpa = rs.getDouble("GPA");
					classification = rs.getString("classification");
					semesterGP += getGradeValue(rs.getString("Grade"));
					semesterCredits += 3;
				}
				
			}
			
			conn.close();
			System.out.println("Closed");
		}
		catch(Exception e)
		{
			System.out.println("there was an exception");
			e.printStackTrace();
		}
	}
	
	public static double getGradeValue(String g)
	{
		g = g.toUpperCase();
		if(g.equals("A"))
		{
			return 4.0;
		}
		else if(g.equals("A-"))
		{
			return 3.67;
		}
		else if(g.equals("B+"))
		{
			return 3.33;
		}
		else if(g.equals("B"))
		{
			return 3.0;
		}
		else if(g.equals("B-"))
		{
			return 2.67;
		}
		else if(g.equals("C+"))
		{
			return 2.33;
		}
		else if(g.equals("C"))
		{
			return 2.0;
		}
		else if(g.equals("C-"))
		{
			return 1.67;
		}
		else if(g.equals("D+"))
		{
			return 1.33;
		}
		else if(g.equals("D"))
		{
			return 1.0;
		}
		else
		{
			return 0;
		}
	}
	
	public static String getClassification(int credits)
	{
		if(credits > 0 && credits < 30)
		{
			return "Freshman";
		}
		else if(credits < 60)
		{
			return "Sophomore";
		}
		else if(credits < 90)
		{
			return "Junior";
		}
		else
		{
			return "Senior";
		}
	}

}
