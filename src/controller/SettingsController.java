package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import model.Disease;
import model.ModeNotSetUpException;

import java.util.ResourceBundle;

public class SettingsController {
    @FXML
    private Label simLbl;
    @FXML
    private Label popSizeLbl;
    @FXML
    private Label iniInfLbl;
    @FXML
    private Label diseaseLbl;
    @FXML
    private Label speedOfSimLbl;
    @FXML
    private Label intRemLbl;
    @FXML
    private Label varLbl;
    @FXML
    private Label maxPplLbl;
    @FXML
    private Label travelTimeLbl;
    @FXML
    private Label calledLbl;
    @FXML
    private Label probCentralLbl;
    @FXML
    private Label quaTimeLbl;
    @FXML
    private Label diseaseLabel;
    @FXML
    private Label diseaseNameLbl;
    @FXML
    private Label infectiousnessLbl;
    @FXML
    private Button saveDiseaseBtn;
    @FXML
    private Button deleteDiseaseBtn;
    @FXML
    private Label popTraitsLbl;
    @FXML
    private Label turnIntLbl;
    @FXML
    private Label TIVarLbl;
    @FXML
    private Label normRadLbl;
    @FXML
    private Label infRadLbl;
    @FXML
    private Slider timeB4QuaSlider;
    @FXML
    private RadioButton quaRad;
    @FXML
    private Slider travelTimeSlider;
    @FXML
    private Slider oneEverySlider;
    @FXML
    private Slider probVisitSlider;
    @FXML
    private Slider intRemSlider;
    @FXML
    private Slider intRemVarSlider;
    @FXML
    private ToggleButton halfSpeed;
    @FXML
    private ToggleButton regularSpeed;
    @FXML
    private ToggleButton doubleSpeed;
    @FXML
    private ToggleGroup toggleSpeed;
    @FXML
    private Slider TIVarSlider;
    @FXML
    private Slider turnIntervalSlider;
    @FXML
    private Slider normRadSlider;
    @FXML
    private Slider infRadSlider;
    @FXML
    private Button setPopBtn;
    @FXML
    private Button editPopBtn;
    @FXML
    private Button setSimBtn;
    @FXML
    private Button editSimBtn;
    @FXML
    private TableView<Disease> diseaseTable;
    @FXML
    private TableColumn<Disease, String> diseaseNameCol;
    @FXML
    private TableColumn<Disease, Double> infectiousnessCol;
    @FXML
    private TextField diseaseNameTf;
    @FXML
    private Slider infectiousnessSlider;
    @FXML
    private Spinner<Integer> pop_sizeSpinner;
    @FXML
    private Spinner<Integer> in_sizeSpinner;
    @FXML
    private ComboBox<Disease> diseaseCombo;
    @FXML
    private RadioButton centralRadio;
    @FXML
    private Spinner<Integer> maxPplSpinner;
    @FXML
    private Button editDBtn;

    //non-FXML components
    boolean isEditing = false;
    SpinnerValueFactory.IntegerSpinnerValueFactory triggerFac;
    ResourceBundle rb = LinkedControllers.rb;

    @FXML
    public void initialize(){
        //setting up the disease Table
        diseaseNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        infectiousnessCol.setCellValueFactory(new PropertyValueFactory<>("infectiousness"));
        diseaseTable.setItems(LinkedControllers.getDiseases());

        // setting up resource bundles
        diseaseLabel.setText(rb.getString("disease"));
        diseaseNameLbl.setText(rb.getString("diseaseName"));
        infectiousnessLbl.setText(rb.getString("infectiousness"));

        saveDiseaseBtn.setText(rb.getString("saveDisease"));
        editDBtn.setText(rb.getString("editDisease"));
        deleteDiseaseBtn.setText(rb.getString("deleteDisease"));

        diseaseNameCol.setText(rb.getString("diseaseName").substring(0, rb.getString("diseaseName").length() - 1));
        infectiousnessCol.setText(rb.getString("infectiousness").substring(0, rb.getString("infectiousness").length() - 1));

        popTraitsLbl.setText(rb.getString("population"));
        turnIntLbl.setText(rb.getString("turnInterval"));
        TIVarLbl.setText(rb.getString("T.IVariation"));
        normRadLbl.setText(rb.getString("normRad"));
        infRadLbl.setText(rb.getString("infectRad"));
        setPopBtn.setText(rb.getString("setPopTraits"));
        editPopBtn.setText(rb.getString("edit"));

        simLbl.setText(rb.getString("sim"));
        popSizeLbl.setText(rb.getString("popSize"));
        iniInfLbl.setText(rb.getString("iniInf"));
        diseaseLbl.setText(rb.getString("diseaseCombo"));
        speedOfSimLbl.setText(rb.getString("speedofSim"));
        intRemLbl.setText(rb.getString("intRem"));
        varLbl.setText(rb.getString("var"));

        centralRadio.setText(rb.getString("central"));
        maxPplLbl.setText(rb.getString("max_ppl"));
        travelTimeLbl.setText(rb.getString("travTime"));
        calledLbl.setText(rb.getString("calledEv"));
        probCentralLbl.setText(rb.getString("probCentral"));

        quaRad.setText(rb.getString("quarantine"));
        quaTimeLbl.setText(rb.getString("TimeB4"));

        setSimBtn.setText(rb.getString("simTraits"));
        editSimBtn.setText(rb.getString("edit"));
        //disable edit buttons
        editPopBtn.setDisable(true);
        editSimBtn.setDisable(true);

        //initialize spinners
        SpinnerValueFactory.IntegerSpinnerValueFactory popValFac = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 50, 1);
        pop_sizeSpinner.setValueFactory(popValFac);

        SpinnerValueFactory.IntegerSpinnerValueFactory infValFac = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 2, 1);
        in_sizeSpinner.setValueFactory(infValFac);

        SpinnerValueFactory.IntegerSpinnerValueFactory maxValFac = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 5, 1);
        maxPplSpinner.setValueFactory(maxValFac);

        //initialize disease combo
        diseaseCombo.setItems(LinkedControllers.getDiseases());

        //assign to LinkControllers.java
        LinkedControllers.half = halfSpeed;
        LinkedControllers.regular = regularSpeed;
        LinkedControllers.doubble = doubleSpeed;

        //disabling the radio buttons

        //load data from resources/popSettings.csv
        LinkedControllers.loadPopData(turnIntervalSlider, TIVarSlider, normRadSlider, infRadSlider);
        //load data from resources/simSettings.csv
        LinkedControllers.loadSimData(pop_sizeSpinner, in_sizeSpinner, diseaseCombo, toggleSpeed, intRemSlider, intRemVarSlider);
        //load data from resources/central.csv
        LinkedControllers.loadCentralData(centralRadio, maxPplSpinner, travelTimeSlider, oneEverySlider, probVisitSlider);
        //load data from resources/quarantine.csv
        LinkedControllers.loadQuarantineData(quaRad, timeB4QuaSlider);

        disableEverything();

    }

    public void disableEverything(){
        setDisablePop(true);
        setDisableBaseSim(true);
        setDisableCentral(true);
        setDisableQuarantine(true);
        centralRadio.setDisable(true);
        quaRad.setDisable(true);
        setSimBtn.setDisable(true);
        setPopBtn.setDisable(true);
        editSimBtn.setDisable(false);
        editPopBtn.setDisable(false);
    }

    @FXML
    public void createDisease(ActionEvent event) {
        if (!isEditing){
            LinkedControllers.addDisease(diseaseNameTf.getText(), Math.round(infectiousnessSlider.getValue() * 10)/10.0, true);
            diseaseNameTf.clear();
            infectiousnessSlider.adjustValue(0);
        }
        else {
            Disease selected = diseaseTable.getSelectionModel().getSelectedItem();
            String newName = diseaseNameTf.getText();
            diseaseNameTf.clear();
            double newInfectiousness = Math.round(infectiousnessSlider.getValue() * 10)/10.0;
            infectiousnessSlider.setValue(0);
            LinkedControllers.editDisease(selected, newName, newInfectiousness);
            diseaseTable.setDisable(false);
            diseaseTable.refresh();
            editDBtn.setDisable(false);
            isEditing = false;
        }
    }

    @FXML
    public void editDisease(ActionEvent event) {
        Disease selected = diseaseTable.getSelectionModel().getSelectedItem();
        if (selected == null){
            LinkedControllers.throwInvalidAlert(rb.getString("noSelected"), rb.getString("diseaseMustBeSelected"));
            return;
        }
        diseaseNameTf.setText(selected.getName());
        infectiousnessSlider.setValue(selected.getInfectiousness());
        diseaseTable.setDisable(true);
        editDBtn.setDisable(true);
        isEditing = true;
    }

    @FXML
    public void deleteDisease(ActionEvent event) {
        Disease selected = diseaseTable.getSelectionModel().getSelectedItem();
        if (selected == null){
            LinkedControllers.throwInvalidAlert(rb.getString("noSelected"), rb.getString("diseaseMustBeSelected"));
            return;
        }
        if (LinkedControllers.getDiseases().size() == 1){
            LinkedControllers.throwInvalidAlert(rb.getString("commandDeny"), rb.getString("comDePrompt"));
            return;
        }
        LinkedControllers.deleteDisease(selected);
        diseaseTable.refresh();
        diseaseCombo.getSelectionModel().select(0);
        setSimulationProperties(event);
    }

    @FXML
    public void setPopulationTraits(ActionEvent event) {
        LinkedControllers.atomicBooleanMain.set(false);
        long turnInterval = (long) (turnIntervalSlider.getValue() * 1000);
        long turnIntervalVariation = (long) (TIVarSlider.getValue() * 1000);
        double radiusOfInfection = infRadSlider.getValue();
        double radius = normRadSlider.getValue();
        if (radiusOfInfection < radius){
            LinkedControllers.throwInvalidAlert(rb.getString("invalPopParam"), rb.getString("radius<norm"));
            return;
        }
        if (radiusOfInfection == 0 || radius == 0){
            LinkedControllers.throwInvalidAlert(rb.getString("invalPopParam"), rb.getString("radius<0"));
            return;
        }
        if (turnInterval < turnIntervalVariation) {
            LinkedControllers.throwInvalidAlert(rb.getString("invalPopParam"), rb.getString("turn<var"));
            return;
        }
        if (!LinkedControllers.throwConfirmationAlert(rb.getString("simReset"), rb.getString("simResetPrompt"))){
            return;
        }
        LinkedControllers.setPopParam(turnInterval, turnIntervalVariation, radiusOfInfection, radius);
        setPopBtn.setDisable(true);
        editPopBtn.setDisable(false);
        setDisablePop(true);

        LinkedControllers.simulation.resetSim();
        LinkedControllers.startButton.setDisable(false);
    }

    @FXML
    public void setSimulationProperties(ActionEvent event){
        //base sim
        LinkedControllers.atomicBooleanMain.set(false);
        double divider = 1;
        int pop_size = pop_sizeSpinner.getValue();
        int in_size = in_sizeSpinner.getValue();
        long intervalRemoved = (long) intRemSlider.getValue();
        long intervalRemVar = (long) intRemVarSlider.getValue();
        Disease selectedDisease = diseaseCombo.getSelectionModel().getSelectedItem();
        ToggleButton selectedToggle = (ToggleButton) toggleSpeed.getSelectedToggle();
        if (selectedDisease == null){
            LinkedControllers.throwInvalidAlert(rb.getString("uncompleteParam"), rb.getString("uncompleteParamPrompt"));
            return;
        }
        if (selectedToggle == null){
            LinkedControllers.throwInvalidAlert(rb.getString("uncompleteParam"), rb.getString("uncompleteParamPrompt"));
            return;
        }

        if (selectedToggle == halfSpeed) divider = 0.5;
        else if (selectedToggle == regularSpeed) divider = 1;
        else if (selectedToggle == doubleSpeed) divider =  2;

        //central spot
        if (centralRadio.isSelected()){
            int max_per_time = maxPplSpinner.getValue();
            double travelTime = travelTimeSlider.getValue();
            double one_every = oneEverySlider.getValue();
            double centralProp = probVisitSlider.getValue();

            LinkedControllers.setUpCentral(max_per_time, travelTime, one_every, centralProp);
        }
        else {
            try{
                LinkedControllers.setFalseCentral();
            }
            catch(ModeNotSetUpException ignored){ }
        }

        //quarantine
        if (quaRad.isSelected()){
//            int triggerPop = triggerInfSpinner.getValue();
            long timeB4 = (long) timeB4QuaSlider.getValue();
//            long timeVar = (long) timeB4VarSlider.getValue();

            LinkedControllers.setUpQuarantine(timeB4);
        }
        else{
            try{
                LinkedControllers.setFalseQuarantine();
            }
            catch(ModeNotSetUpException ignored){}
        }

        //done

        if (!LinkedControllers.throwConfirmationAlert(rb.getString("simReset"), rb.getString("simResetPrompt"))){
            return;
        }
        LinkedControllers.updateSim(selectedDisease, pop_size, in_size, intervalRemoved, intervalRemVar, divider);
        
        setSimBtn.setDisable(true);
        editSimBtn.setDisable(false);
        setDisableBaseSim(true);
        setDisableCentral(true);
        centralRadio.setDisable(true);
        setDisableQuarantine(true);
        quaRad.setDisable(true);
        LinkedControllers.simulation.resetSim();
        LinkedControllers.startButton.setDisable(false);
    }

    @FXML
    public void editPopSettings(ActionEvent event) {
        if (LinkedControllers.atomicBooleanMain.get() && LinkedControllers.throwConfirmationAlert(rb.getString("simStop"), rb.getString("simStopPrompt"))){
            LinkedControllers.atomicBooleanMain.set(false);
        }
        setDisablePop(false);
        setPopBtn.setDisable(false);
        editPopBtn.setDisable(true);
    }

    @FXML
    public void editSimSettings(ActionEvent event) {
        if (LinkedControllers.atomicBooleanMain.get() && LinkedControllers.throwConfirmationAlert(rb.getString("simStop"), rb.getString("simStopPrompt"))){
            LinkedControllers.atomicBooleanMain.set(false);
        }
        setDisableBaseSim(false);
        setDisableCentral(!centralRadio.isSelected());
        setDisableQuarantine(!quaRad.isSelected());
        setSimBtn.setDisable(false);
        editSimBtn.setDisable(true);
        centralRadio.setDisable(false);
        quaRad.setDisable(false);
    }

    @FXML
    public void checkValidPop(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER){
            try{
                Integer.parseInt(pop_sizeSpinner.getEditor().getText());
            }
            catch (NumberFormatException e){
                pop_sizeSpinner.getEditor().setText("50");
            }
        }
    }

    @FXML
    public void checkValidInf(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER){
            try{
                Integer.parseInt(in_sizeSpinner.getEditor().getText());
            }
            catch (NumberFormatException e){
                in_sizeSpinner.getEditor().setText("2");
            }
        }
    }

    @FXML
    public void checkValidMaxPep(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER){
            try{
                Integer.parseInt(maxPplSpinner.getEditor().getText());
            }
            catch (NumberFormatException e){
                maxPplSpinner.getEditor().setText("5");
            }
        }
    }

    @FXML
    public void quarantineDisable(ActionEvent event) {
        setDisableQuarantine(!quaRad.isSelected());
    }

    @FXML
    public void centralDisable(ActionEvent event) {
        setDisableCentral(!centralRadio.isSelected());
    }
    //non-FXML methods
    private void setDisablePop(boolean isDisabled){
        turnIntervalSlider.setDisable(isDisabled);
        TIVarSlider.setDisable(isDisabled);
        normRadSlider.setDisable(isDisabled);
        infRadSlider.setDisable(isDisabled);
    }

    private void setDisableBaseSim(boolean isDisabled){
        pop_sizeSpinner.setDisable(isDisabled);
        in_sizeSpinner.setDisable(isDisabled);
        diseaseCombo.setDisable(isDisabled);
        halfSpeed.setDisable(isDisabled);
        regularSpeed.setDisable(isDisabled);
        doubleSpeed.setDisable(isDisabled);
        intRemSlider.setDisable(isDisabled);
        intRemVarSlider.setDisable(isDisabled);
    }

    private void setDisableCentral(boolean isDisabled){
        maxPplSpinner.setDisable(isDisabled);
        travelTimeSlider.setDisable(isDisabled);
        oneEverySlider.setDisable(isDisabled);
        probVisitSlider.setDisable(isDisabled);
    }

    private void setDisableQuarantine(boolean isDisabled){
        timeB4QuaSlider.setDisable(isDisabled);
    }
}
