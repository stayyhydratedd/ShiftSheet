package ru.stayyhydratedd.wbot.ShiftSheet.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
public class AppFolder {

    @Id
    private int id;

    @Column(name = "title")
    @Getter
    private String title;

    @Column(name = "google_id")
    @Getter
    @Setter
    private String googleId;

    @Column(name = "users_folder_google_id")
    @Getter
    @Setter
    private String usersFolderGoogleId;

    @Column(name = "user_data_spreadsheet_google_id")
    @Getter
    @Setter
    private String userDataSpreadsheetGoogleId;

    @Column(name = "user_details_sheet_google_id")
    @Getter
    @Setter
    private Integer userDetailsSheetGoogleId;

    @Column(name = "roots_folder_google_id")
    @Getter
    @Setter
    private String rootsFolderGoogleId;
}
