package com.eduai.eduai_backend.service;

import com.eduai.eduai_backend.entity.Question;
import com.eduai.eduai_backend.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DataSeederService implements CommandLineRunner {

    private final QuestionRepository questionRepository;

    @Override
    public void run(String... args) {
        if (questionRepository.count() > 0) return;

        questionRepository.saveAll(List.of(
                // Java Questions
                Question.builder()
                        .topic("Java").difficulty("Easy")
                        .questionText("Which of these is not a Java primitive type?")
                        .optionA("int").optionB("String")
                        .optionC("boolean").optionD("char")
                        .correctAnswer("B")
                        .explanation("String is a class in Java, not a primitive type. Primitives are int, long, double, float, boolean, char, byte, short.")
                        .build(),
                Question.builder()
                        .topic("Java").difficulty("Medium")
                        .questionText("What is the output of: System.out.println(10/3)?")
                        .optionA("3.33").optionB("3")
                        .optionC("4").optionD("Compile error")
                        .correctAnswer("B")
                        .explanation("Integer division in Java truncates the decimal. 10/3 = 3 (not 3.33). Use 10.0/3 for decimal result.")
                        .build(),
                Question.builder()
                        .topic("Java").difficulty("Medium")
                        .questionText("Which keyword is used to prevent method overriding?")
                        .optionA("static").optionB("abstract")
                        .optionC("final").optionD("private")
                        .correctAnswer("C")
                        .explanation("The 'final' keyword on a method prevents subclasses from overriding it.")
                        .build(),
                Question.builder()
                        .topic("Java").difficulty("Hard")
                        .questionText("What does the 'volatile' keyword do in Java?")
                        .optionA("Makes variable constant").optionB("Ensures thread visibility")
                        .optionC("Prevents null assignment").optionD("Speeds up execution")
                        .correctAnswer("B")
                        .explanation("'volatile' ensures that changes to a variable are visible to all threads immediately by reading from main memory instead of CPU cache.")
                        .build(),
                Question.builder()
                        .topic("Java").difficulty("Medium")
                        .questionText("Which collection does NOT allow duplicate elements?")
                        .optionA("ArrayList").optionB("LinkedList")
                        .optionC("HashSet").optionD("Vector")
                        .correctAnswer("C")
                        .explanation("HashSet implements the Set interface which does not allow duplicates. ArrayList, LinkedList and Vector all allow duplicates.")
                        .build(),

                // DSA Questions
                Question.builder()
                        .topic("DSA").difficulty("Easy")
                        .questionText("What is the time complexity of binary search?")
                        .optionA("O(n)").optionB("O(n²)")
                        .optionC("O(log n)").optionD("O(1)")
                        .correctAnswer("C")
                        .explanation("Binary search halves the search space each step, giving O(log n) time complexity. It requires the array to be sorted.")
                        .build(),
                Question.builder()
                        .topic("DSA").difficulty("Medium")
                        .questionText("Which data structure uses LIFO order?")
                        .optionA("Queue").optionB("Stack")
                        .optionC("LinkedList").optionD("Tree")
                        .correctAnswer("B")
                        .explanation("Stack uses LIFO (Last In First Out). The last element pushed is the first to be popped.")
                        .build(),
                Question.builder()
                        .topic("DSA").difficulty("Hard")
                        .questionText("What is the worst case time complexity of QuickSort?")
                        .optionA("O(n log n)").optionB("O(n)")
                        .optionC("O(n²)").optionD("O(log n)")
                        .correctAnswer("C")
                        .explanation("QuickSort worst case is O(n²) when the pivot is always the smallest or largest element (already sorted array). Average case is O(n log n).")
                        .build(),
                Question.builder()
                        .topic("DSA").difficulty("Medium")
                        .questionText("Which traversal of a BST gives elements in sorted order?")
                        .optionA("Preorder").optionB("Postorder")
                        .optionC("Level order").optionD("Inorder")
                        .correctAnswer("D")
                        .explanation("Inorder traversal (Left → Root → Right) of a Binary Search Tree always gives elements in ascending sorted order.")
                        .build(),
                Question.builder()
                        .topic("DSA").difficulty("Easy")
                        .questionText("What is the space complexity of an array of size n?")
                        .optionA("O(1)").optionB("O(log n)")
                        .optionC("O(n)").optionD("O(n²)")
                        .correctAnswer("C")
                        .explanation("An array of n elements requires O(n) space since each element occupies memory proportional to n.")
                        .build(),

                // SQL Questions
                Question.builder()
                        .topic("SQL").difficulty("Easy")
                        .questionText("Which SQL command retrieves data from a table?")
                        .optionA("INSERT").optionB("UPDATE")
                        .optionC("SELECT").optionD("DELETE")
                        .correctAnswer("C")
                        .explanation("SELECT is used to retrieve/query data from one or more tables in SQL.")
                        .build(),
                Question.builder()
                        .topic("SQL").difficulty("Medium")
                        .questionText("Which JOIN returns all rows from both tables?")
                        .optionA("INNER JOIN").optionB("LEFT JOIN")
                        .optionC("RIGHT JOIN").optionD("FULL OUTER JOIN")
                        .correctAnswer("D")
                        .explanation("FULL OUTER JOIN returns all rows from both tables, with NULL where there is no match.")
                        .build(),
                Question.builder()
                        .topic("SQL").difficulty("Medium")
                        .questionText("What does GROUP BY do in SQL?")
                        .optionA("Sorts results").optionB("Filters rows")
                        .optionC("Groups rows for aggregation").optionD("Joins tables")
                        .correctAnswer("C")
                        .explanation("GROUP BY groups rows with the same values in specified columns, used with aggregate functions like COUNT, SUM, AVG.")
                        .build(),

                // Spring Boot Questions
                Question.builder()
                        .topic("Spring Boot").difficulty("Easy")
                        .questionText("Which annotation marks a class as a REST controller?")
                        .optionA("@Controller").optionB("@RestController")
                        .optionC("@Service").optionD("@Component")
                        .correctAnswer("B")
                        .explanation("@RestController combines @Controller and @ResponseBody, making every method return JSON/XML directly without needing @ResponseBody on each.")
                        .build(),
                Question.builder()
                        .topic("Spring Boot").difficulty("Medium")
                        .questionText("Which annotation is used for dependency injection in Spring?")
                        .optionA("@Inject").optionB("@Resource")
                        .optionC("@Autowired").optionD("All of the above")
                        .correctAnswer("D")
                        .explanation("All three — @Autowired (Spring), @Inject (JSR-330), and @Resource (JSR-250) — can be used for dependency injection in Spring.")
                        .build()
        ));

        System.out.println("Quiz questions seeded successfully!");
    }
}