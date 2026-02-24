import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.annotation.Annotation;

public class Test {
    public static void main(String[] args) throws Exception {
        Method m = ResponseEntityExceptionHandler.class.getDeclaredMethod(
            "handleMethodArgumentNotValid", 
            org.springframework.web.bind.MethodArgumentNotValidException.class, 
            org.springframework.http.HttpHeaders.class, 
            org.springframework.http.HttpStatusCode.class, 
            org.springframework.web.context.request.WebRequest.class
        );
        System.out.println("Return type annotations:");
        for(Annotation a : m.getAnnotations()) {
            System.out.println("  " + a);
        }
        for (int i = 0; i < m.getParameters().length; i++) {
            Parameter p = m.getParameters()[i];
            System.out.println("Param " + i + " annotations:");
            for(Annotation a : p.getAnnotations()) {
                System.out.println("  " + a);
            }
        }
    }
}
