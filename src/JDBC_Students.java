import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class JDBC_Students {
	
	public static void main(String[] args) throws IOException
	{
		// Part A
		String outputFile = "JDBC_StudentsOutput.txt";
		File f = new File(outputFile);
		PrintWriter out = new PrintWriter(new FileWriter(f)); 
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
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

			int id = 0;
			int credits = 0;
			double gpa = 0.0;
			String classification = "";
			int semesterCredits = 0;
			double semesterGP = 0;
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
						String outString = Integer.toString(id) + "\t" + df.format(gpa) +  "\t" + credits + "\t" + classification;
						out.println(outString);
						out.flush();
						semesterGP = 0;
						semesterCredits = 0;
					}
					else
					{
						first = false;
					}
					
					// get information about the new student
					id = newid;
					credits = rs.getInt("CreditHours");
					gpa = rs.getDouble("GPA");
					classification = rs.getString("Classification").trim();
					semesterGP += getGradeValue(rs.getString("Grade").trim());
					semesterCredits += 3;
				}
				
			}
			out.close();
			
			// Part B
			// get top 5 seniors
			System.out.println("getting top 5");
			statement = conn.createStatement();
			rs = statement.executeQuery("select senior.Name as seniorName, mentor.Name as mentorName, GPA from Student s inner join Person as senior on s.StudentID = senior.ID inner join Person as mentor on mentor.ID = s.MentorID where s.Classification = 'Senior' order by GPA");
			outputFile = "P3Output.txt";
			f = new File(outputFile);
			out = new PrintWriter(new FileWriter(f)); 
			
			System.out.println("made rs");
			ArrayList<ArrayList<String>> info = new ArrayList<ArrayList<String>>();
			double[] topGPAs = new double[5];
			for(int i = 0; i < 5; i++)
			{
				topGPAs[i] = 0;
				ArrayList<String> temp = new ArrayList<String>();
				info.add(temp);
			}
			while(rs.next())
			{
				gpa = rs.getDouble("GPA");
				String newInfo = rs.getString("seniorName") + "\t" + rs.getString("mentorName") + "\t" + df.format(gpa);
				//highest gpa first
				for(int i = 0; i < 5; i++)
				{
					if(gpa > topGPAs[i])
					{
						for(int j = 4; j > i; j--)
						{
							topGPAs[j] = topGPAs[j-1];
							info.set(j, info.get(j-1));							
						}
						topGPAs[i] = gpa;
						info.set(i, new ArrayList<String>());
						info.get(i).add(newInfo);
						i = 10;
					}
					else if(gpa == topGPAs[i])
					{
						info.get(i).add(newInfo);
						i = 10;
					}
				}
			}
			
			for(int i = 0; i < 5; i++)
			{
				for(String str : info.get(i))
				{
					System.out.println(str);
					out.println(str);
					out.flush();
				}
			}
			
			conn.close();
			out.close();
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
