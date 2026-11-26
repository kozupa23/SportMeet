package cz.osu.sportmeet;

import org.jspecify.annotations.NullMarked;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the SportMeet backend.
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
@SpringBootApplication
public class SportMeetApplication
{
    static void main(String[] args)
    {
        SpringApplication.run(SportMeetApplication.class, args);
    }
}
