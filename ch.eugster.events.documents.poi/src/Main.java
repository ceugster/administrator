import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import pl.jsolve.templ4docx.core.Docx;
import pl.jsolve.templ4docx.variable.BulletListVariable;
import pl.jsolve.templ4docx.variable.ImageVariable;
import pl.jsolve.templ4docx.variable.TableVariable;
import pl.jsolve.templ4docx.variable.TextVariable;
import pl.jsolve.templ4docx.variable.Variable;
import pl.jsolve.templ4docx.variable.Variables;

public class Main
{
    public static final void main(String[] args)
    {
    	Main main = new Main();
    	main.fillTable();
    }
    
    public void fillTable() 
    {
        Docx docx = new Docx(System.getProperty("user.home") + "\\student.docx");
        Variables var = new Variables();

        TableVariable tableVariable = new TableVariable();

        List<Variable> nameColumnVariables = new ArrayList<Variable>();
        List<Variable> ageColumnVariables = new ArrayList<Variable>();
        List<Variable> logoColumnVariables = new ArrayList<Variable>();
        List<Variable> languagesColumnVariables = new ArrayList<Variable>();

        List<Student> students = getStudents();
            for(Student student : students) {
            	           nameColumnVariables.add(new TextVariable("${name}", student.getName()));
           ageColumnVariables.add(new TextVariable("${age}", student.getAge().toString()));
           logoColumnVariables.add(new ImageVariable("${logo}", student.getLogoPath(), 40, 40));

           List<Variable> languages = new ArrayList<Variable>();
           for(String language : student.getLanguages()) {
               languages.add(new TextVariable("${languages}", language));
           }
           languagesColumnVariables.add(new BulletListVariable("${languages}", languages));
        }

        tableVariable.addVariable(nameColumnVariables);
        tableVariable.addVariable(ageColumnVariables);
        tableVariable.addVariable(logoColumnVariables);
        tableVariable.addVariable(languagesColumnVariables);

        var.addTableVariable(tableVariable);
        docx.fillTemplate(var);
        docx.save(System.getProperty("user.home") + "\\" + UUID.randomUUID().toString() + ".docx");
    }

    private List<Student> getStudents() 
    {
        List<Student> students = new ArrayList<Student>();
        students.add(new Student("Lukasz", 28, "money.png", Arrays.asList("Polish", "English")));
        students.add(new Student("Tomek", 24, "money.png", Arrays.asList("Polish", "English", "French")));
        return students;
    }

    static class Student
    {
        private String name;
        private Integer age;
        private String logoPath;
        private List<String> languages;

        public Student(String name, Integer age, String logoPath, List<String> languages)
        {
	        this.name = name;
	        this.age = age;
	        this.logoPath = logoPath;
	        this.languages = languages;
        }

        public String getName() {
            return name;
        }

        public Integer getAge() {
            return age;
        }

        public String getLogoPath() {
            return logoPath;
        }

        public List<String> getLanguages() {
            return languages;
        }

    }
}

