module vidmot.boids {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens vidmot.boids to javafx.fxml;
    exports vidmot.boids;
}
