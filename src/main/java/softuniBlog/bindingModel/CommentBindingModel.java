package softuniBlog.bindingModel;


import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CommentBindingModel {

    @Size(min = 5, max= 50, message = "Title must be between 5 and 50 characters.")
    @NotBlank(message = "Title must be at least 20 characters long.")
    private String title;

    @DateTimeFormat(pattern="dd/MM/yyyy HH:mm:ss")
    private String localDateTime;

    @NotBlank(message = "Content may not be empty.")
    @Size(min = 20, message = "Content must be at least 20 characters long.")
    private String content;

    private Integer articleId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public String getLocalDateTime() {

        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public void setLocalDateTime(String localDateTime) {
        this.localDateTime = localDateTime;
    }
}