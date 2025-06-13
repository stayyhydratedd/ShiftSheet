package ru.stayyhydratedd.wbot.ShiftSheet.services;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.dtos.AuthUserDTO;
import ru.stayyhydratedd.wbot.ShiftSheet.dtos.RegisterUserDTO;
import ru.stayyhydratedd.wbot.ShiftSheet.mappers.UserMapper;
import ru.stayyhydratedd.wbot.ShiftSheet.models.AppFolder;
import ru.stayyhydratedd.wbot.ShiftSheet.models.RootFolder;
import ru.stayyhydratedd.wbot.ShiftSheet.models.User;
import ru.stayyhydratedd.wbot.ShiftSheet.repositories.UserRepository;
import ru.stayyhydratedd.wbot.ShiftSheet.security.MyUserDetails;
import ru.stayyhydratedd.wbot.ShiftSheet.services.google.GoogleService;
import ru.stayyhydratedd.wbot.ShiftSheet.util.GoogleFileWorkerUtil;
import ru.stayyhydratedd.wbot.ShiftSheet.util.JWTUtil;

import java.io.IOException;
import java.util.*;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final Sheets sheetsService;
    private final SessionContext sessionContext;
    private final GoogleFileWorkerUtil googleFileWorkerUtil;
    private final JWTUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder,
                       GoogleService googleService, SessionContext sessionContext,
                       GoogleFileWorkerUtil googleFileWorkerUtil , JWTUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.googleFileWorkerUtil = googleFileWorkerUtil;
        this.sessionContext = sessionContext;
        this.sheetsService = googleService.getSheetsService();
        this.jwtUtil = jwtUtil;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException(String.format("Пользователь с именем '%s' не найден", username)));

        return new MyUserDetails(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(int id){
        return userRepository.findByIdWithRootFolders(id);
    }

    public List<User> findAllByRootFoldersContaining(RootFolder rootFolder) {
        return userRepository.findAllByRootFoldersContaining(rootFolder);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveWithPasswordEncoding(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User saveWithoutPasswordEncoding(User user) {
        return userRepository.save(user);
    }

    public User registerUserDtoToUser(RegisterUserDTO registerUserDTO) {
        return userMapper.registerUserDtoToUser(registerUserDTO);
    }

    public User authUserDtoToUser(AuthUserDTO authUserDTO) {
        return userMapper.authUserDtoToUser(authUserDTO);
    }

    public AuthUserDTO registerUserDtoToAuthUserDto(RegisterUserDTO registerUserDTO) {
        return userMapper.registerUserDtoToAuthUserDto(registerUserDTO);
    }

    public int executeFormatUserDataSheet(String userDataSpreadsheetId){
        try {
            List<Sheet> sheets = googleFileWorkerUtil.getSheets(userDataSpreadsheetId);
            Sheet firstSheet = sheets.getFirst();  // нулевой по индексу
            int sheetId = firstSheet.getProperties().getSheetId();

            List<Request> requests = new ArrayList<>(){{
                add(new Request().setUpdateSheetProperties(
                        new UpdateSheetPropertiesRequest().setProperties(
                                new SheetProperties()
                                        .setSheetId(sheetId)
                                        .setTitle("userDetails")
                                        .setGridProperties(
                                                new GridProperties()
                                                        .setRowCount(1)
                                                        .setColumnCount(1)
                                        )
                        ).setFields("title,gridProperties(rowCount,columnCount)")
                ));
                add(new Request().setUpdateDimensionProperties(
                        new UpdateDimensionPropertiesRequest()
                                .setRange(new DimensionRange()
                                        .setSheetId(sheetId)
                                        .setDimension("COLUMNS")
                                        .setStartIndex(0)
                                        .setEndIndex(1)
                                )
                                .setProperties(new DimensionProperties()
                                        .setPixelSize(1330) // ширина в пикселях
                                )
                                .setFields("pixelSize")
                ));
                add(new Request().setUpdateCells(
                        new UpdateCellsRequest()
                                .setRows(List.of(
                                        new RowData().setValues(List.of(
                                                new CellData().setUserEnteredValue(
                                                        new ExtendedValue().setStringValue("token"))
                                        ))
                                ))
                                .setFields("*")
                                .setStart(new GridCoordinate()
                                        .setSheetId(sheetId)
                                        .setRowIndex(0)       // строка 0 => первая строка (A1, B1, C1)
                                        .setColumnIndex(0)    // столбец 0
                                )
                ));
            }};
            Random random = new Random();

            Color randomColor = new Color()
                    .setRed(random.nextFloat(0.8f, 0.99f))
                    .setGreen(random.nextFloat(0.8f, 0.99f))
                    .setBlue(random.nextFloat(0.8f, 0.99f));

            requests.add(new Request().setRepeatCell(
                    new RepeatCellRequest()
                            .setCell(new CellData().setUserEnteredFormat(
                                    new CellFormat()
                                            .setBackgroundColor(randomColor)
                                            .setTextFormat(new TextFormat().setFontSize(12))
                                            .setHorizontalAlignment("CENTER")
                                            .setVerticalAlignment("MIDDLE")
                            ))
                            .setRange(new GridRange()
                                    .setSheetId(sheetId)
                                    .setStartRowIndex(0)
                                    .setEndRowIndex(1)
                                    .setStartColumnIndex(0)
                                    .setEndColumnIndex(1)
                            )
                            .setFields("userEnteredFormat.backgroundColor" +
                                    ",userEnteredFormat.textFormat" +
                                    ",userEnteredFormat.horizontalAlignment" +
                                    ",userEnteredFormat.verticalAlignment")
            ));

            googleFileWorkerUtil.executeRequestsBody(requests, userDataSpreadsheetId);
            return sheetId;
        } catch (IOException e){
            e.printStackTrace();
            return 0;
        }
    }

    public void appendUserTokenToSheet(User user) {
        AppFolder appFolder = sessionContext.getAppFolder();

        String token = jwtUtil.generateToken(user);

        List<List<Object>> values = List.of(
                List.of(token)
        );
        ValueRange body = new ValueRange().setValues(values);

        try{
            sheetsService.spreadsheets().values()
                    .append(appFolder.getUserDataSpreadsheetGoogleId(), "userDetails", body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
