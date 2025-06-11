import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Deal2 extends Application implements EventHandler<ActionEvent> {

    private int[][] gameBoard;
    private GridPane g1;
    private Button[][] b1;
    private int numColumns;
    private int numRows;
    private int balance;
    private Label balanceLabel;
    private Stage stage1;
    private int clickCount;
    private List<Integer> shuffledNumbers;
    private List<Integer> shuffledAmounts;
    private List<Integer> valuesLeft;
    private int personalCaseNumber;
    private int personalCaseValue;


    @Override
    public void start(Stage stage) {
        this.stage1 = stage;
        numColumns = 4;
        numRows = 4;
        balance = 0;

        // main layout
        VBox mainLayout = new VBox();
        balanceLabel = new Label("Balance: $0");
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> resetGame());

        gameBoard = new int[numRows][numColumns];
        //grid setup
        g1 = new GridPane();
        g1.setHgap(10);
        g1.setVgap(10);
        g1.setStyle("-fx-padding: 15;");

        b1 = new Button[numRows][numColumns];

        // start game
        gameStart();

        mainLayout.getChildren().addAll(balanceLabel, g1, resetButton);

        Scene scene = new Scene(mainLayout);
        stage1.setScene(scene);
        stage1.setTitle("Deal Game");
        stage1.show();
    }

    // initial numbers and prize amounts
    public void gameStart() {
        shuffledNumbers = new ArrayList<>();
        for (int i = 1; i <= numRows * numColumns; i++) {
            shuffledNumbers.add(i);
        }
        Collections.shuffle(shuffledNumbers);

        int[] cashAmounts = {1, 5, 10, 25, 50, 75, 100, 200, 500, 700, 1000, 5000, 10000, 50000, 100000, 1000000};
        shuffledAmounts = new ArrayList<>();
        for (int amount : cashAmounts) {
            shuffledAmounts.add(amount);
        }
        Collections.shuffle(shuffledAmounts);

        clickCount = 0;
        valuesLeft = new ArrayList<>(shuffledAmounts);

        int index = 0;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                int number = shuffledNumbers.get(index++);
                gameBoard[i][j] = number;

                // Create styled button
                Button button = new Button(Integer.toString(number));
                button.setPrefSize(120, 120);
                button.setStyle(
                        "-fx-background-color: linear-gradient(#d3d3d3, #a9a9a9);" +
                                "-fx-text-fill: black;" +
                                "-fx-font-size: 18px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 10;" +
                                "-fx-border-color: gray;" +
                                "-fx-border-width: 2;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 4, 0.3, 0, 3);"
                );

                b1[i][j] = button;
                g1.add(button, j, i);
                button.setOnAction(this);
            }
        }
    }

    @Override
    public void handle(ActionEvent e) {
        //sets up buttons with corresponding amounts
        Button clickedButton = (Button) e.getSource();
        int number = Integer.parseInt(clickedButton.getText());
        int index = shuffledNumbers.indexOf(number);
        int price = shuffledAmounts.get(index);

        if (clickCount != 0) {
            //setup for adding balance for every button click
            clickedButton.setText("$" + price);
            clickedButton.setDisable(true);
            balance += price;
            valuesLeft.remove(Integer.valueOf(price));
            updateBalanceDisplay();
        }
        else {
            //personal case setup
            clickedButton.setStyle("-fx-background-color: #FFD700; -fx-text-fill: black;");

            personalCaseNumber = number;
            personalCaseValue = price;
            clickedButton.setDisable(true);
            System.out.println("This is your personal case: Case #" + personalCaseNumber);
        }

        clickCount++;
        //first 15 then choose 4 more
        //11 then choose 3 more
        //8 then choose 3 more
        //5 then choose 2 more
        //3 then choose 2 more (not including personal case and last case)
        if (clickCount == 5 || clickCount == 8 || clickCount == 11 || clickCount == 13 || clickCount == 15) {
            bankOffer1();
        }
        if (clickCount == 15) {
            System.out.println("You can open your personal case or the last case on the board");
            //if new button clicked is

            int lastCaseNumber = -1;
            for (int i = 0; i < numRows; i++) {
                for (int j=0; j < numColumns; j++) {
                    if (!b1[i][j].isDisabled()) {
                        lastCaseNumber = Integer.parseInt(b1[i][j].getText());
                    }
                }
            }
            int lastCaseValue = shuffledAmounts.get(shuffledNumbers.indexOf(lastCaseNumber));

            //final alert
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("last round");
            alert.setHeaderText("Keep your personal case or Swap?");

            //setting buttons
            ButtonType keep = new ButtonType("Keep");
            ButtonType swap = new ButtonType("Swap");
            alert.getButtonTypes().setAll(keep,swap);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == keep) {
                System.out.println("The last case had:" + lastCaseValue);
                System.out.println("You win: $" + personalCaseValue);
            }
            if (result.isPresent() && result.get() == swap) {
                System.out.println("Your personal case had:" + personalCaseValue);
                System.out.println("You win: $" + lastCaseValue);

            }

        }
    }

    private void updateBalanceDisplay() {
        balanceLabel.setText("Balance: $" + balance);
    }

    private void resetGame() {
        balance = 0;
        updateBalanceDisplay();
        g1.getChildren().clear();
        gameStart();
    }

    // first bank offers based on expected value
    private void bankOffer1() {
        double expectedValue = calculateExpectedValue();

        bankOfferHelper(expectedValue);
    }



    // Calculate the expected value
    private double calculateExpectedValue() {
        int sum = 0;
        for (int amount : valuesLeft) {
            sum += amount;
        }
        return sum / (double)(numColumns * numRows - clickCount) * 0.8;
    }

    // Display the bank offer with expected value
    public void bankOfferHelper(double expectedValue) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Bank Offer: $" + String.format("%.2f", expectedValue));
        alert.setContentText("Do you accept the offer?");

        //add custom yes or no buttons
        ButtonType accept = new ButtonType("Accept");
        ButtonType reject = new ButtonType("Reject");
        alert.getButtonTypes().setAll(accept, reject);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == accept) {
            System.out.println("You win $" + expectedValue);
            System.out.println("Your personal case had:" + personalCaseValue);
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    Button b2= b1[i][j];
                    if (!b2.isDisabled()) {
                        int number = Integer.parseInt(b2.getText());
                        int index = shuffledNumbers.indexOf(number);
                        int price = shuffledAmounts.get(index);
                        b2.setText("$" + price);
                        b2.setDisable(true);
                    }
                }
            }
        } else if (result.isPresent() && result.get() == reject) {
            System.out.println("Let's move on to the next round!");
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}

