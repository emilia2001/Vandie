package com.example.socialnetworkfinal;

import com.example.socialnetworkfinal.domain.Page;
import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.service.EventService;
import com.example.socialnetworkfinal.service.FriendshipService;
import com.example.socialnetworkfinal.service.MessageService;
import com.example.socialnetworkfinal.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ProfileController extends PageController{
    @FXML
    TextField lastNameField;
    @FXML
    TextField firstNameField;
    @FXML
    TextField emailField;
    @FXML
    TextField nrFriendsField;
    @FXML
    Circle profilePicture;
    @FXML
    Rectangle coverPicture;
    @FXML
    AnchorPane allPane;

    public void init() {
        lastNameField.setText(page.getLastName());
        firstNameField.setText(page.getFirstName());
        emailField.setText(page.getEmail());
        nrFriendsField.setText(String.valueOf(page.getFriends().size()));
        try {
            coverPicture.setFill(new ImagePattern(new Image(new FileInputStream(user.getCoverPictureUrl()))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Image image = new Image(new FileInputStream(user.getProfilePictureUrl()));
            profilePicture.setFill(new ImagePattern(image));
            profilePicture.setSmooth(true);
            profilePicture.setCache(true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        allPane.getChildren().get(0).toBack();
        centralPane.getChildren().get(0).toBack();

    }

    @Override
    public void setService(User user, Long id, Page page, UserService userService, FriendshipService friendshipService, MessageService messageService, EventService eventService) {
        super.setService(user, id, page, userService, friendshipService, messageService, eventService);
        init();
    }

    public void handleReports() {
        this.friendshipService.removeObserver(this);
        FXMLLoader loader = openWindow("ReportsView.fxml", (Stage) logoutButton.getScene().getWindow(),"Reports" );
        ReportsController controller = loader.getController();
        controller.setService(user, userId, page, userService, friendshipService, messageService, eventService);
    }

    public void handleUpdateProfilePic() {
        Stage secondStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(secondStage);
        if(selectedFile != null) {
            String pictureUrl = selectedFile.toURI().toString();
            Image image = new Image(pictureUrl);
            ImagePattern imagePattern= new ImagePattern(image);
            profilePicture.setFill(imagePattern);
            profilePicture.setSmooth(true);
            profilePicture.setCache(true);
            String picUrl = "src\\main\\resources\\com\\example\\socialnetworkfinal\\images\\profilePics\\";
            picUrl += selectedFile.getName();
            try {
                Files.copy(selectedFile.toPath(), Path.of(picUrl), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            user.setProfilePictureUrl(picUrl);
            userService.updateUser(user);
        }
        secondStage.close();
    }

    public void handleUpdateCoverPic() {
        Stage secondStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(secondStage);
        if(selectedFile != null) {
            String coverUrl = selectedFile.toURI().toString();
            Image image = new Image(coverUrl);
            ImagePattern imagePattern= new ImagePattern(image);
            coverPicture.setFill(imagePattern);
            coverPicture.setSmooth(true);
            coverPicture.setCache(true);
            String picUrl = "src\\main\\resources\\com\\example\\socialnetworkfinal\\images\\coverPics\\";
            picUrl += selectedFile.getName();
            try {
                Files.copy(selectedFile.toPath(), Path.of(picUrl), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            user.setCoverPictureUrl(picUrl);
            userService.updateUser(user);
        }
        secondStage.close();
    }
}
