package vidmot.boids;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import vinnsla.Boid;
import vinnsla.Hakarl;
import vinnsla.Vec2D;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * `BoidController` stýrir hegðun og samskiptum viðmótsþátta fyrir Boid herma.
 * Hann sér um að frumstilla og uppfæra Boid, stjórna hreyfingu hákarls og
 * skipta á milli mismunandi hamstilla (Jaws og Thobbyverse).
 */
public class BoidController {

    // Listar sem geyma Boids fyrir mismunandi hópa.
    private List<Boid> gruppa1Boids = new ArrayList<>();
    private List<Boid> gruppa2Boids = new ArrayList<>();
    private List<Boid> gruppa3Boids = new ArrayList<>();
    private List<Boid> gruppa4Boids = new ArrayList<>();

    // MediaPlayer hlutir til að spila bakgrunnstónlist og hljóðáhrif.
    private MediaPlayer bakgrunnstonlistPlayer;
    private MediaPlayer japlPlayer;
    private MediaPlayer jawsPlayer;

    @FXML
    private Pane pane; // FXML skilgreining fyrir viðmótshluta sem heldur á Boids og hákarli.

    @FXML
    private Label label; // FXML skilgreining fyrir merki sem sýnir stöðu leikjarins.

    // Breytur til að geyma hákarl og Boid hluti.
    private Hakarl hakarl;
    private Boid boid;

    // Boolean breytur til að halda utan um hamstillur (Jaws og Thobbyverse).
    private boolean jaws = false;
    private boolean thobbyverse = false;

    // Breytur til að fylgjast með fjölda borðaðra fiska og hvort leikurinn er lokið.
    private int count = 0;
    private boolean leiklokið;

    /**
     * Athugar hvort leikurinn sé lokið.
     *
     * @return true ef leikurinn er lokið, annars false.
     */
    public boolean isLeiklokið() {
        return leiklokið;
    }

    /**
     * Setur leiklokið breytuna.
     *
     * @param leiklokið true ef leikurinn er lokið, annars false.
     */
    public void setLeiklokið(boolean leiklokið) {
        this.leiklokið = leiklokið;
    }

    /**
     * Athugar hvort Jaws hamur sé virkur.
     *
     * @return true ef Jaws hamur er virkur, annars false.
     */
    public boolean isJaws() {
        return jaws;
    }

    /**
     * Athugar hvort Thobbyverse hamur sé virkur.
     *
     * @return true ef Thobbyverse hamur er virkur, annars false.
     */
    public boolean isThobbyverse() {
        return thobbyverse;
    }

    /**
     * Setur Thobbyverse haminn.
     *
     * @param t true ef Thobbyverse hamur á að vera virkur, annars false.
     */
    public void setThobbyverse(boolean t) {
        this.thobbyverse = t;
    }

    /**
     * Setur Jaws haminn.
     *
     * @param j true ef Jaws hamur á að vera virkur, annars false.
     */
    public void setJaws(boolean j) {
        this.jaws = j;
    }

    /**
     * Smiður fyrir `BoidController` klasann.
     */
    public BoidController() {
    }

    /**
     * Frumstillir viðmótshluta þegar BoidController er ræstur.
     * Spilar bakgrunnstónlist, frumstillir Boids og hákarl.
     */
    @FXML
    public void initialize() {
        // Kóði til að hlaða og spila tónlist
        URL tonlist = getClass().getResource("/vidmot/boids/Tonlist/fishyfiles.mp3");
        Media bakgrunnsTonlist = new Media(tonlist.toString());
        bakgrunnstonlistPlayer = new MediaPlayer(bakgrunnsTonlist);
        bakgrunnstonlistPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        bakgrunnstonlistPlayer.play();

        // Hlaða hljóðáhrifum
        URL japlLeid = getClass().getResource("/vidmot/boids/Tonlist/ate.mp3");
        Media japl = new Media(japlLeid.toString());
        japlPlayer = new MediaPlayer(japl);

        URL jawsleid = getClass().getResource("/vidmot/boids/Tonlist/jaws.mp3");
        Media jaws = new Media(jawsleid.toString());
        jawsPlayer = new MediaPlayer(jaws);

        // Setur upp forritið á JavaFX vlási
        Platform.runLater(() -> {
            frumstillaBoids();
            initalizeHakarl();
            pane.setOnKeyPressed(this::handleKeyPress);
            pane.setFocusTraversable(true);
        });
    }

    /**
     * Frumstillir hákarl og bætir honum við í viðmótið.
     */
    public void initalizeHakarl() {
        Vec2D sharkStadsetning = new Vec2D(pane.getWidth() / 2, pane.getHeight() / 2);
        hakarl = new Hakarl(sharkStadsetning, "/vidmot/boids/myndir/hakarl.png", pane.getWidth(), pane.getHeight());
        pane.getChildren().add(hakarl.getMynd());
    }

    /**
     * Sér um að vinna úr lyklaborðsinslækkum sem notandinn framkvæmir til að stjórna hákarlinum
     * eða skipta á milli hamstilla.
     *
     * @param event KeyEvent sem lýsir hvaða lykill var notaður.
     */
    private void handleKeyPress(KeyEvent event) {
        double speed = 1.0;  //Hraði hákarlsins
        Vec2D nyrHradi = new Vec2D(0, 0);

        // Athugar hvaða lykill var notaður og breytir hraða hákarlsins eða skiptir um ham.
        switch (event.getCode()) {
            case UP:
                nyrHradi.y -= speed;
                break;
            case DOWN:
                nyrHradi.y += speed;
                break;
            case LEFT:
                nyrHradi.x -= speed;
                break;
            case RIGHT:
                nyrHradi.x += speed;
                break;
            case SPACE:
                toggleJawsMode();
                break;
            case TAB:
                toggleThobbyverseMode();
                break;
        }

        hakarl.setHradi(nyrHradi);
    }

    /**
     * Breytir myndum og stærð allra Boids í viðmótinu.
     *
     * @param imagePath Stígur að nýju myndinni fyrir Boids.
     * @param staerd    Ný stærð Boids.
     */
    private void breytaOllumFiskum(String imagePath, int staerd) {
        List<List<Boid>> allGroups = List.of(gruppa1Boids, gruppa2Boids, gruppa3Boids, gruppa4Boids);
        for (List<Boid> group : allGroups) {
            for (Boid boid : group) {
                boid.bennar(imagePath, staerd);
            }
        }
    }

    /**
     * Skiptir á milli Thobbyverse ham og venjulegs hams. Breytir myndum Boids og hákarls í samræmi við það.
     */
    private void toggleThobbyverseMode() {
        if (!isThobbyverse()) {
            setThobbyverse(true);
            breytaOllumFiskum("/vidmot/boids/myndir/beni.png", 100);
            hakarl.jaws("/vidmot/boids/myndir/thobby.png");

        } else {
            setThobbyverse(false);
            breytaOllumFiskum("/vidmot/boids/myndir/bluefish", 40);
            hakarl.jaws("/vidmot/boids/myndir/hakarl.png");
        }
    }

    /**
     * Skiptir á milli Jaws ham og venjulegs hams. Stoppar bakgrunnstónlistina og spilar Jaws hljóðáhrif í samræmi við það.
     */
    private void toggleJawsMode() {
        if (!isJaws()) {
            setJaws(true);
            bakgrunnstonlistPlayer.pause();
            hakarl.jaws("/vidmot/boids/myndir/shark.png");
            jawsPlayer.play();
        } else {
            setJaws(false);
            jawsPlayer.pause();
            hakarl.jaws("/vidmot/boids/myndir/hakarl.png");
            bakgrunnstonlistPlayer.play();
        }
    }

    /**
     * Frumstillir alla Boid hópa og byrjar á hreyfingu þeirra.
     */
    public void frumstillaBoids() {
        byrjaGruppu(gruppa1Boids, "/vidmot/boids/myndir/brownfish.png", 10);
        byrjaGruppu(gruppa2Boids, "/vidmot/boids/myndir/bluefish.png", 50);
        byrjaGruppu(gruppa3Boids, "/vidmot/boids/myndir/orangeFish.png", 40);
        byrjaGruppu(gruppa4Boids, "/vidmot/boids/myndir/pufferFish.png", 15);

        hefjaAnimation();
    }

    /**
     * Frumstillir tiltekinn Boid hóp með gefinni mynd og fjölda Boids.
     *
     * @param group  Listi til að geyma Boids fyrir tiltekinn hóp.
     * @param mynd   Stígur að mynd fyrir Boids.
     * @param fjoldi Fjöldi Boids í hópnum.
     */
    private void byrjaGruppu(List<Boid> group, String mynd, int fjoldi) {

        for (int i = 0; i < fjoldi; i++) {
            double x = Math.random() * pane.getWidth();
            double y = Math.random() * pane.getHeight();
            Vec2D stadsetning = new Vec2D(x, y);
            Vec2D hradi = new Vec2D(Math.random() * 2 - 1, Math.random() * 2 - 1);
            Boid boid = new Boid(stadsetning, hradi, mynd, pane.getWidth(), pane.getHeight());
            group.add(boid);
            pane.getChildren().add(boid.getImageView());
        }

    }

    /**
     * Byrjar á hreyfingu Boids og uppfærir þau reglulega.
     */
    private void hefjaAnimation() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isLeiklokið()) {
                    uppfaeraSimulation(gruppa1Boids, 10);
                    uppfaeraSimulation(gruppa2Boids, 50);
                    uppfaeraSimulation(gruppa3Boids, 40);
                    uppfaeraSimulation(gruppa4Boids, 15);
                    if (getCount() >= 115) {
                        setLeiklokið(true);
                        stop();
                        if (isThobbyverse()) {
                            sigurThobby();
                        } else {
                            sigur();
                        }
                    }
                }
            }
        };
        timer.start();
    }

    /**
     * Uppfærir stöðu og hreyfingu Boids í tilteknum hóp og athugar hvort hákarl hafi borðað þau.
     *
     * @param boids  Listi yfir Boids í hópnum.
     * @param fjoldi Fjöldi Boids í hópnum.
     */
    private void uppfaeraSimulation(List<Boid> boids, int fjoldi) {
        if (isLeiklokið()) {
            return;
        }
        int loopLimit = Math.min(fjoldi, boids.size());
        for (int i = loopLimit - 1; i >= 0; i--) {
            Boid boid = boids.get(i);
            double fjarlaegd = boid.getStadsetning().fjarlaegd(hakarl.getStadsetning());
            if (fjarlaegd < hakarl.getAtFjarlaegd()) {
                spilaJapl();
                pane.getChildren().remove(boid.getImageView());
                boids.remove(i);
                int tala = getCount();
                tala++;
                setCount(tala);
                if (isThobbyverse()) {
                    label.setText("Kærastar borðaðir: " + getCount());
                } else {
                    label.setText("Fiskar borðaðir: " + getCount());
                }

                if (getCount() % 10 == 0) {
                    if (!isJaws() && !isThobbyverse()) {
                        hakarl.staekkun("/vidmot/boids/myndir/hakarl.png");
                    } else if (isJaws()) {
                        hakarl.staekkun("/vidmot/boids/myndir/shark.png");
                    } else if (isThobbyverse()) {
                        hakarl.staekkun("/vidmot/boids/myndir/thobby.png");
                    }
                }
            }
            if (i < boids.size()) {
                boid.update(boids, hakarl);
                boid.hreyfa();
                boid.uppfaeraMynd();
            }
        }
        hakarl.hreyfa();
    }

    /**
     * Sýnir skilaboð um sigur þegar leikurinn er kláraður og leikmaðurinn hefur borðað alla fiskana.
     */
    private void sigur() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sigur");
            alert.setHeaderText(null);
            alert.setContentText("Þú ást alla fiskana!");
            alert.showAndWait();
        });
    }

    /**
     * Sýnir skilaboð um sigur í Thobbyverse ham þegar leikmaðurinn hefur borðað alla kærastana.
     */
    private void sigurThobby() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Flott");
            alert.setHeaderText(null);
            alert.setContentText("Þú ást alla Kærastana!");
            alert.showAndWait();
        });
    }

    /**
     * Setur fjölda borðaðra fiska.
     *
     * @param count Fjöldi borðaðra fiska.
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Sækir fjölda borðaðra fiska.
     *
     * @return Fjöldi borðaðra fiska.
     */
    public int getCount() {
        return count;
    }

    /**
     * Spilar hljóðáhrif þegar hákarl borðar Boid.
     */
    private void spilaJapl() {
        if (japlPlayer != null) {
            if (japlPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                japlPlayer.stop();
            }
            japlPlayer.play();
        } else {
            System.out.println("Mediaplayer hófst ekki");
        }
    }
}
