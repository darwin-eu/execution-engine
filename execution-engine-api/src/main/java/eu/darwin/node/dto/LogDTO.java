package eu.darwin.node.dto;

import eu.darwin.node.domain.Log;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@AllArgsConstructor
@Getter
public class LogDTO {

    private String level;
    private Date date;
    private String line;

    public static LogDTO fromEntity(Log entity) {
        return new LogDTO(entity.level(), entity.date(), entity.line());
    }
}
