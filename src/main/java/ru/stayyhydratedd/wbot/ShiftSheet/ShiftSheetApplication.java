package ru.stayyhydratedd.wbot.ShiftSheet;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.stayyhydratedd.wbot.ShiftSheet.configurations.GoogleContextConfig;

@SpringBootApplication
@RequiredArgsConstructor
public class ShiftSheetApplication {

	private static final AnnotationConfigApplicationContext googleContext =
			new AnnotationConfigApplicationContext(GoogleContextConfig.class);

	public static void main(String[] args) {
		googleContext.start();
		SpringApplication.run(ShiftSheetApplication.class, args);
	}
}
