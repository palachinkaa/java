package softuniBlog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import softuniBlog.bindingModel.CommentBindingModel;
import softuniBlog.entity.Article;
import softuniBlog.entity.Comment;
import softuniBlog.entity.User;
import softuniBlog.repository.ArticleRepository;
import softuniBlog.repository.CommentRepository;
import softuniBlog.repository.UserRepository;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Controller
public class CommentController {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private UserRepository userRepository;


    @GetMapping("/comments/{id}/create")
    @PreAuthorize("isAuthenticated()")
    public String addComment(Model model, @PathVariable Integer id){

        if(!this.articleRepository.exists(id)){
            return "redirect:/";
        }

        if(!(SecurityContextHolder.getContext().getAuthentication()
                instanceof AnonymousAuthenticationToken)){
            UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();

            User userEntity = this.userRepository.findByEmail(principal.getUsername());

            model.addAttribute("user", userEntity);
        }

        Article article = this.articleRepository.findOne(id);
        Set<Comment> comments = article.getComments();

        model.addAttribute("comments", comments);
        model.addAttribute("view", "comments/create");
        model.addAttribute("article", article);

        return "base-layout";
    }
    @PostMapping("/comments/{id}/create")
    @PreAuthorize("isAuthenticated()")
    public String addCommentProcess(@PathVariable Integer id, @Valid CommentBindingModel commentBindingModel, BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes){
        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("errors", bindingResult.getFieldErrors());
            return "redirect:/comments/{id}/create";
        }
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Article article = this.articleRepository.findOne(id);
        User userEntity = this.userRepository.findByEmail(user.getUsername());

        Comment comment = new Comment(commentBindingModel.getTitle(),
                commentBindingModel.getContent(),
                userEntity,
                article,
                commentBindingModel.getLocalDateTime());

        this.commentRepository.saveAndFlush(comment);

        return "redirect:/article/" + id;
    }
    @GetMapping("/comments/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Integer id, Model model){
        if (!this.commentRepository.exists(id)) {
            return "redirect:/";
        }

        Comment comment = this.commentRepository.findOne(id);

        if (!isUserAuthorOrAdmin(comment)) {
            return "redirect:/comments/edit" + id;
        }

        model.addAttribute("view", "comments/edit");
        model.addAttribute("comment", comment);

        return "base-layout";
    }
    @PostMapping("/comments/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String editProcess(@PathVariable Integer id, @Valid CommentBindingModel commentBindingModel, BindingResult bindingResult,
                              RedirectAttributes redirectAttributes){
        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("errors", bindingResult.getFieldErrors());
            return "redirect:/comments/edit/" + id;
        }
        Comment comment = this.commentRepository.findOne(id);
        if(!isUserAuthorOrAdmin(comment)){
            return "redirect:/comments/" + id;
        }



        comment.setContent(commentBindingModel.getContent());
        comment.setTitle(commentBindingModel.getTitle());
        comment.setLocalDateTime(commentBindingModel.getLocalDateTime());
        this.commentRepository.saveAndFlush(comment);
        return "redirect:/article/" + comment.getArticle().getId();
    }
    @GetMapping("/comments/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(@PathVariable Integer id, Model model){
        if(!this.commentRepository.exists(id)){
            return "redirect:/";
        }

        Comment comment = this.commentRepository.findOne(id);

        if(!this.isUserAuthorOrAdmin(comment)){
            return "redirect:/";
        }

        model.addAttribute("view", "comments/delete");
        model.addAttribute("comment", comment);

        return "base-layout";
    }
    @PostMapping("/comments/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteProcess(@PathVariable Integer id, CommentBindingModel commentBindingModel) {

        if(!this.commentRepository.exists(id)){
            return "redirect:/";
        }

        Comment comment = this.commentRepository.findOne(id);

        if(!this.isUserAuthorOrAdmin(comment)){
            return "redirect:/";
        }

        Article article = this.articleRepository.findOne(comment.getArticle().getId());

        this.commentRepository.delete(id);

        return "redirect:/article/" + article.getId();
    }


    private boolean isUserAuthorOrAdmin(Comment comment){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User userEntity = this.userRepository.findByEmail(user.getUsername());



        return userEntity.isAdmin() || userEntity.isAuthor(comment);
    }



}
