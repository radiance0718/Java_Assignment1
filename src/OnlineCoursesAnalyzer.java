import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class OnlineCoursesAnalyzer {

	private final List<Course> courses = new ArrayList<>();

	public OnlineCoursesAnalyzer(String datasetPath) {
		BufferedReader br = null;
		String line;
		try {
			br = new BufferedReader(new FileReader(datasetPath));
			br.readLine();
			while ((line = br.readLine()) != null) {
				String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
				Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
						Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
						Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
						Double.parseDouble(info[12]), Double.parseDouble(info[13]), Double.parseDouble(info[14]),
						Double.parseDouble(info[15]), Double.parseDouble(info[16]), Double.parseDouble(info[17]),
						Double.parseDouble(info[18]), Double.parseDouble(info[19]), Double.parseDouble(info[20]),
						Double.parseDouble(info[21]), Double.parseDouble(info[22]));
				courses.add(course);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//1
	public Map<String, Integer> getPtcpCountByInst() {
		Map<String, Integer> m1 = courses.stream().sorted(
				Comparator.comparing(course -> course.institution)
		).filter(
				course -> course.institution != null
		).collect(
				Collectors.groupingBy(
						course -> course.institution,
						Collectors.reducing(
								0, course -> course.participants, Integer::sum
						)));

		m1 = m1.entrySet().stream().sorted(
				Map.Entry.comparingByKey()
		).collect(
				Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(oldValue, newValue) -> oldValue, LinkedHashMap::new
				));
		System.out.println(m1.size());
		for (String i : m1.keySet()) {
			if (m1.get(i) != null)
				System.out.println(i + "=" + m1.get(i).toString());
			else
				System.out.println(i + "=[]");
		}
		return m1;
	}

	//2
	public Map<String, Integer> getPtcpCountByInstAndSubject() {
		Map<String, Integer> m2 = courses.stream().collect(
				Collectors.groupingBy(
						course -> course.institution + "-" + course.subject,
						Collectors.reducing(0, course -> course.participants, Integer::sum)
				)
		);

		m2 = m2.entrySet().stream().sorted(
				Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder())
		).collect(Collectors.toMap(
				Map.Entry::getKey,
				Map.Entry::getValue,
				(oldValue, newValue) -> oldValue, LinkedHashMap::new
		));
		return m2;
	}

	//3
	public Map<String, List<List<String>>> getCourseListOfInstructor() {
		HashMap<String, Boolean> vis = new HashMap<>();
		List<String> instructor = new ArrayList<>();
		for (int i = 0; i < courses.size(); i++) {
			String s = courses.get(i).instructors;
			String[] strlist = s.split(",");
			for (int j = 0; j < strlist.length; j++) {
				strlist[j] = strlist[j].trim();
				if (vis.get(strlist[j]) == null) {
					instructor.add(strlist[j]);
					vis.put(strlist[j], Boolean.TRUE);
				}
			}
		}
		Map<String, List<List<String>>> m3 = new HashMap<>();
		for (int i = 0; i < instructor.size(); i++) {
			int p = i;
			List<String> s1 = courses.stream().filter(
					course -> {
						boolean res2 = course.instructors.equals(instructor.get(p));
						return res2;
					}
			).map(
					course -> course.title
			).sorted(
			).distinct().collect(
					Collectors.toList()
			);

			List<String> s2 = courses.stream().filter(
					course -> {
						boolean res2 = course.instructors.equals(instructor.get(p));
						if (res2) return false;
						String str = course.instructors;
						String[] ls = str.split(",");
						for (int j = 0; j < ls.length; j++) {
							ls[j] = ls[j].trim();
							if (ls[j].equals(instructor.get((p)))) {
								return true;
							}
						}
						return false;
					}
			).map(
					course -> course.title
			).sorted(
			).distinct().collect(
					Collectors.toList()
			);
			List<List<String>> s3 = new ArrayList<>();
			s3.add(s1);
			s3.add(s2);
			m3.put(instructor.get(i), s3);

		}

		return m3;
	}

	//4
	public List<String> getCourses(int topK, String by) {
		Map<String, Integer> m4 = courses.stream().sorted(
				Comparator.comparing(course -> course.institution)
		).collect(
				Collectors.groupingBy(
						course -> course.title,
						Collectors.reducing(0, course -> course.participants, Integer::max)));
		List<String> l1;
		if (by.equals("hours")) {
			l1 = courses.stream().sorted(
					Comparator.comparingDouble(
							Course::getTotalHours
					).reversed().thenComparing(Course::getTitle)
			).map(
					course -> course.title
			).distinct().limit(topK).collect(
					Collectors.toList()
			);
		} else {
			l1 = m4.entrySet().stream().sorted(
					(a, b) -> {
						if (Objects.equals(a.getValue(), b.getValue())) {
							return b.getKey().compareTo(a.getKey());
						}
						return b.getValue() - a.getValue();
					}
			).map(
					Map.Entry::getKey
			).distinct().limit(topK).collect(
					Collectors.toList()
			);
		}
		for (int i = 0; i < l1.size(); i++) {
			System.out.println(l1.get(i));
		}
		return l1;
	}

	//5
	public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
		List<String> s5 = courses.stream().filter(
				course -> {
					boolean res1 = course.subject.toLowerCase(Locale.ROOT).contains(courseSubject.toLowerCase(Locale.ROOT));
					boolean res2 = (course.percentAudited >= percentAudited);
					boolean res3 = (course.totalHours <= totalCourseHours);
					return res1 && res2 && res3;
				}
		).map(
				course -> course.title
		).distinct().sorted().collect(Collectors.toList());
		return s5;
	}

	//6
	public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
		Map<String, Double> m2 = courses.stream().collect(
				Collectors.groupingBy(
						course -> course.number,
						Collectors.summingDouble(Course::getMedianAge)
				)
		);
		Map<String, Double> m3 = courses.stream().collect(
				Collectors.groupingBy(
						course -> course.number,
						Collectors.summingDouble(Course::getPercentMale)
				)
		);
		Map<String, Double> m4 = courses.stream().collect(
				Collectors.groupingBy(
						course -> course.number,
						Collectors.summingDouble(Course::getPercentDegree)
				)
		);

		Map<String, List<Course>> m1 = courses.stream().collect(
				Collectors.groupingBy(
						course -> course.number,
						Collectors.toList()
				)
		);

		List<String> l6 = m1.entrySet().stream().sorted(
				(a, b) -> {
					String c1 = a.getKey();
					String c2 = b.getKey();
					double a1 = m2.get(c1) / a.getValue().size();
					double a2 = m3.get(c1) / a.getValue().size();
					double a3 = m4.get(c1) / a.getValue().size();
					double gen100 = gender * 100;
					double or100 = isBachelorOrHigher * 100;
					double k1 = (age - a1) * (age - a1) + (gen100 - a2) * (gen100 - a2) + (or100 - a3) * (or100 - a3);
					double b1 = m2.get(c2) / b.getValue().size();
					double b2 = m3.get(c2) / b.getValue().size();
					double b3 = m4.get(c2) / b.getValue().size();
					double k2 = (age - b1) * (age - b1) + (gen100 - b2) * (gen100 - b2) + (or100 - b3) * (or100 - b3);
					if (k1 - k2 > 0) return 1;
					return -1;
				}
		).map(
				a -> {
					String p = a.getKey();
					System.out.println(a.getKey() + m2.get(p).toString() + " " + m3.get(p).toString() + " " + m4.get(p).toString());
					String str = null;
					Date d = null;
					for (int i = 0; i < a.getValue().size(); i++) {
						if (str == null) {
							d = a.getValue().get(i).launchDate;
							str = a.getValue().get(i).title;
							continue;
						}
						if (a.getValue().get(i).launchDate.after(d)) {
							d = a.getValue().get(i).launchDate;
							str = a.getValue().get(i).title;
						}
					}
					return str;
				}
		).distinct().limit(10).collect(Collectors.toList());
//
//        for(int i = 0;i < 10;i++){
//            System.out.println(l6.get(i));
//        }

		return l6;
	}


}

class Course {
	String institution;
	String number;
	Date launchDate;
	String title;
	String instructors;
	String subject;
	int year;
	int honorCode;
	int participants;
	int audited;
	int certified;
	double percentAudited;
	double percentCertified;
	double percentCertified50;
	double percentVideo;
	double percentForum;
	double gradeHigherZero;
	double totalHours;
	double medianHoursCertification;
	double medianAge;
	double percentMale;
	double percentFemale;
	double percentDegree;

	public Course(String institution, String number, Date launchDate,
				  String title, String instructors, String subject,
				  int year, int honorCode, int participants,
				  int audited, int certified, double percentAudited,
				  double percentCertified, double percentCertified50,
				  double percentVideo, double percentForum, double gradeHigherZero,
				  double totalHours, double medianHoursCertification,
				  double medianAge, double percentMale, double percentFemale,
				  double percentDegree) {
		this.institution = institution;
		this.number = number;
		this.launchDate = launchDate;
		if (title.startsWith("\"")) title = title.substring(1);
		if (title.endsWith("\""))
			title = title.substring(0, title.length() - 1);
		this.title = title;
		if (instructors.startsWith("\"")) instructors = instructors.substring(1);
		if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
		this.instructors = instructors;
		if (subject.startsWith("\"")) subject = subject.substring(1);
		if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
		this.subject = subject;
		this.year = year;
		this.honorCode = honorCode;
		this.participants = participants;
		this.audited = audited;
		this.certified = certified;
		this.percentAudited = percentAudited;
		this.percentCertified = percentCertified;
		this.percentCertified50 = percentCertified50;
		this.percentVideo = percentVideo;
		this.percentForum = percentForum;
		this.gradeHigherZero = gradeHigherZero;
		this.totalHours = totalHours;
		this.medianHoursCertification = medianHoursCertification;
		this.medianAge = medianAge;
		this.percentMale = percentMale;
		this.percentFemale = percentFemale;
		this.percentDegree = percentDegree;
	}

	public double getTotalHours() {
		return this.totalHours;
	}

	public String getTitle() {
		return this.title;
	}

	public int getParticipants() {
		return participants;
	}

	public double getPercentMale() {
		return percentMale;
	}

	public double getMedianAge() {
		return medianAge;
	}

	public double getPercentDegree() {
		return percentDegree;
	}
}