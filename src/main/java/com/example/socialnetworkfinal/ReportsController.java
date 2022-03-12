package com.example.socialnetworkfinal;

import com.example.socialnetworkfinal.domain.DTOMessage;
import com.example.socialnetworkfinal.domain.DTOUser;
import com.example.socialnetworkfinal.domain.Page;
import com.example.socialnetworkfinal.domain.User;
import com.example.socialnetworkfinal.repository.paging.PageableRepoImplementation;
import com.example.socialnetworkfinal.service.EventService;
import com.example.socialnetworkfinal.service.FriendshipService;
import com.example.socialnetworkfinal.service.MessageService;
import com.example.socialnetworkfinal.service.UserService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.contentstream.PDContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportsController extends PageController{
    @FXML
    DatePicker beginDate;
    @FXML
    DatePicker endDate;
    @FXML
    TableColumn<DTOUser, String> nameColumn;
    @FXML
    TableColumn<DTOUser, Image> pictureColumn;
    @FXML
    Button nextButton;
    @FXML
    Button prevButton;
    @FXML
    TableView<DTOUser> friendsTable;
    ObservableList<DTOUser> friends = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<DTOUser, String>("name"));
        pictureColumn.setCellFactory(param -> {
            Circle circle = new Circle();
            circle.setRadius(15);
            TableCell<DTOUser, Image> cell = new TableCell<DTOUser, Image>() {
                public void updateItem(Image item, boolean empty) {
                    if (item != null) {
                        circle.setVisible(true);
                        circle.setFill( new ImagePattern(item));
                    }else
                        circle.setVisible(false);
                }
            };
            cell.setGraphic(circle);
            return cell;
        });
        pictureColumn.setCellValueFactory(new PropertyValueFactory<DTOUser, Image>("picture"));
        friendsTable.setItems(friends);
        Label holder = new Label("You have no friends");
        holder.setTextFill(Color.WHITE);
        friendsTable.setPlaceholder(holder);
        prevButton.setVisible(false);
    }

    @Override
    public void setService(User user, Long id, Page page, UserService userService, FriendshipService friendshipService, MessageService messageService, EventService eventService) {
        super.setService(user, id, page, userService, friendshipService, messageService, eventService);
        userService.setPageable(new PageableRepoImplementation(0, 3));
        page.getFriends().clear();
        page.getFriends().addAll(userService.getFriendsOnPage(0, userId));
        List<DTOUser> friends = getDtoUser();
        if( this.friends.isEmpty() )
            this.friends.setAll(friends);
        if(userService.getNextFriends(userId).isEmpty()) {
            nextButton.setVisible(false);
        } else
            userService.getPrevFriends(userId);
    }

    public List<DTOUser> getDtoUser() {
        return page.getFriends()
                .stream()
                .map(u -> {
                    try {
                        return new DTOUser(u.getId(), u.getFirstName() + " " + u.getLastName(), new Image(new FileInputStream(u.getProfilePictureUrl())));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.toList());
    }

    public void getNewFriends(LocalDate begin, LocalDate end, File location) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        document.save(location);
        document.close();
        document = PDDocument.load(location);
        page = document.getPage(0);
        PDPageContentStream stream = new PDPageContentStream(document, page);
        stream.beginText();
        stream.setLeading(16f);
        stream.newLineAtOffset(25, 700);
        stream.setFont(PDType1Font.COURIER_BOLD_OBLIQUE, 18);
        String text = "    NEW FRIENDS";
        System.out.println(text);
        stream.showText(text);
        stream.newLine();
        stream.setFont(PDType1Font.COURIER, 14);
        stream.setLeading(15f);
        PDPageContentStream finalStream = stream;
        friendshipService.getApprovedRequestsInInterval(userId, begin, end).forEach(f -> {
            try {
                finalStream.showText(f.getFirstName() + " " + f.getLastName() + " since " + f.getDate());
                finalStream.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        stream.endText();
        stream.close();

        page = new PDPage();
        document.addPage(page);stream = new PDPageContentStream(document, page);
        stream.beginText();
        stream.setLeading(16f);
        stream.newLineAtOffset(25, 700);
        stream.setFont(PDType1Font.COURIER_BOLD_OBLIQUE, 18);
        text = "    RECEIVED MESSAGES";
        stream.showText(text);
        stream.newLine();
        stream.setFont(PDType1Font.COURIER, 14);
        stream.setLeading(15f);
        PDPageContentStream finalStream1 = stream;
        messageService.getReceivedMessagesInInterval(userId, LocalDateTime.of(begin.getYear(), begin.getMonth(), begin.getDayOfMonth(), 00, 00, 00), LocalDateTime.of(end.getYear(), end.getMonth(), end.getDayOfMonth(), 00, 00, 00))
            .forEach(m -> {
            try {
                String text2 = "From " + m.getFrom() + " at " + m.getDate().format(DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss")) + ": " + m.getText();
                String[] desc = insertEndLine(text2, 69).split("\n");
                finalStream1.showText(desc[0]);
                finalStream1.newLine();
                if(desc.length > 1) {
                    finalStream1.showText(desc[1]);
                    finalStream1.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        stream.endText();
        stream.close();
        document.save(location);
        document.close();
    }

    public static String insertString(String originalString, String stringToBeInserted, int index) {
        String newString = originalString.substring(0, index + 1)
                + stringToBeInserted
                + originalString.substring(index + 1);
        return newString;
    }

    public static String insertEndLine(String string, int index){
        String newString = string;
        if(string.length() > index)
            for(int i = index; i< string.length(); i=i + index)
                newString = insertString(newString,"\n",i);
        return  newString;
    }

    public void handleSaveReports() throws IOException {
        Stage secondStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PDF Files", ".pdf"));
        File location = fileChooser.showSaveDialog(secondStage);
        if(location != null)
            getNewFriends(beginDate.getValue(), endDate.getValue(), location);
    }


    public void handleSaveMessages() throws IOException{
        DTOUser user = friendsTable.getSelectionModel().getSelectedItem();
        if(user != null) {
            friendsTable.getSelectionModel().clearSelection();
            Stage secondStage = new Stage();
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PDF Files", ".pdf"));
            File location = fileChooser.showSaveDialog(secondStage);
            if (location != null)
                getUserReceivedMessages(beginDate.getValue(), endDate.getValue(), user, location);
        }
    }

    private void getUserReceivedMessages(LocalDate begin, LocalDate end, DTOUser user, File location) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        document.save(location);
        document.close();
        document = PDDocument.load(location);
        page = document.getPage(0);
        PDPageContentStream stream = new PDPageContentStream(document, page);
        stream.beginText();
        stream.setLeading(16f);
        stream.newLineAtOffset(25, 700);
        stream.setFont(PDType1Font.COURIER_BOLD_OBLIQUE, 18);
        String text = "    MESSAGES FROM ";
        text += user.getName().toUpperCase(Locale.ROOT);
        stream.showText(text);
        stream.newLine();
        stream.setFont(PDType1Font.COURIER, 14);
        stream.setLeading(15f);
        PDPageContentStream finalStream = stream;

        messageService.getReceivedByUserInInterval(userId, user.getId(), LocalDateTime.of(begin.getYear(), begin.getMonth(), begin.getDayOfMonth(), 00, 00, 00), LocalDateTime.of(end.getYear(), end.getMonth(), end.getDayOfMonth(), 00, 00, 00))
                .forEach(m -> {
                    System.out.println(m);
                    try {
                        finalStream.showText("At " + m.getDate().format(DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss")) + ": " + m.getText());
                        finalStream.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });
        stream.endText();
        stream.close();
        document.save(location);
        document.close();
    }


    public void getNextFriends() {
        prevButton.setVisible(true);
        Set<User> nextFriends = userService.getNextFriends(userId);
        page.getFriends().clear();
        page.getFriends().addAll(nextFriends);
        System.out.println(page.getFriends());
        friends.setAll(getDtoUser());
        if(userService.getNextFriends(userId).isEmpty()) {
            nextButton.setVisible(false);
        } else
            userService.getPrevFriends(userId);
    }

    public void getPrevFriends() {
        nextButton.setVisible(true);
        Set<User> nextFriends = userService.getPrevFriends(userId);
        page.getFriends().clear();
        page.getFriends().addAll(nextFriends);
        System.out.println(page.getFriends());
        friends.setAll(getDtoUser());
        if(userService.getPrevFriends(userId).isEmpty()) {
            prevButton.setVisible(false);
        } else
            userService.getNextFriends(userId);
    }

}
