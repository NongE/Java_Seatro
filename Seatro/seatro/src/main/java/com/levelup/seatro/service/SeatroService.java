package com.levelup.seatro.service;

import com.levelup.seatro.database.entity.StationDetailUsers;
import com.levelup.seatro.database.entity.StationUsers;
import com.levelup.seatro.database.entity.SubwayStations;
import com.levelup.seatro.database.emb.SubwayStationsEmb;
import com.levelup.seatro.model.DirectionMapping;
import com.levelup.seatro.repository.StationDetailUsersRepository;
import com.levelup.seatro.repository.StationUsersRepository;
import com.levelup.seatro.repository.SubwayStationsRepository;
import com.levelup.seatro.repository.SurroundStationsRepository;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@Service
public class SeatroService {

    private final SubwayStationsRepository subwayStationsRepository;
    private final StationUsersRepository stationUsersRepository;
    private final StationDetailUsersRepository stationDetailUsersRepository;
    private final SurroundStationsRepository surroundStationsRepository;

    public SeatroService(SubwayStationsRepository subwayStationsRepository, StationUsersRepository stationUsersRepository, StationDetailUsersRepository stationDetailUsersRepository, SurroundStationsRepository surroundStationsRepository) {
        this.subwayStationsRepository = subwayStationsRepository;
        this.stationUsersRepository = stationUsersRepository;
        this.stationDetailUsersRepository = stationDetailUsersRepository;
        this.surroundStationsRepository = surroundStationsRepository;
    }

    public Map<String, Object> getAllSubwayStations() {

        Map<String, Object> result = new HashMap<>();

        List<Map<String, String>> stationList = new ArrayList<>();

        // 테이블에 있는 모든 데이터 조회
        try {

            List<SubwayStations> stations = subwayStationsRepository.findAll();

            stations.forEach(index -> {
                SubwayStationsEmb subwayStationsEmb = index.getSubwayStationsEntityEmb();
                stationList.add(new HashMap() {
                    {
                        put("line_number", subwayStationsEmb.getLineNumber());
                        put("station_name", subwayStationsEmb.getStationName());
                    }
                });

            });

            result.put("status", HttpStatus.OK);
            result.put("data", stationList);

        } catch (Exception e) {
            // 테이블 조회 중 오류 발생 시 임의의 값을 넣어 반환
            stationList.clear();
            stationList.add(new HashMap() {
                {
                    put("line_number", "Not Found!");
                    put("station_name", "Not Found!");
                }
            });

            result.put("status", HttpStatus.OK);
            result.put("data", stationList);

        }

        return result;

    }


    public StationUsers findPopularStation() {
        // 현재 시간 구하기
        LocalTime currentTime = LocalTime.now();

        // 이용객 수를 기준으로 내림차순 정렬, 이후 가장 많은 역사를 반환
        return stationUsersRepository.findByStationUsersEmbCheckInTimeOrderByPeopleDesc(currentTime.getHour()).get(0);

    }


    public List<Map<String, Object>> getStationInfo(String lineNumber, String stationName, int checkInTime) {

        List<Map<String, Object>> result = new ArrayList<>();

        List<StationDetailUsers> stationUsers = stationDetailUsersRepository.findByStationUsersEmbLineNumberAndStationUsersEmbStationNameAndStationUsersEmbCheckInTime(lineNumber, stationName, checkInTime);

        if (stationUsers.size() != 0) {

            stationUsers.forEach(index -> {

                // 정렬을 위해 TreeMap
                Map<String, Object> stationInfo = new TreeMap<>();

                // 역사의 진행 방향을 가져옵니다.
                DirectionMapping getSurroundStation = surroundStationsRepository.findBySurroundStationsEmbLineNumberAndSurroundStationsEmbStationNameAndSurroundStationsEmbDirection(index.getStationUsersEmb().getLineNumber(), index.getStationUsersEmb().getStationName(), index.getStationUsersEmb().getDirection());

                stationInfo.put("direction", getSurroundStation.getSurroundStation());

                stationInfo.put("cabin", index.getAllCabin());

                stationInfo.put("first_time", "05:55");

                stationInfo.put("last_time", "23:55");

                result.add(stationInfo);
            });


        } else{
            // 오류 발생 시 아래로 반환
            Map<String, Object> failResult = new HashMap<>() {{

                put("reason", "결과 없음");
                put("input_line_number", lineNumber);
                put("input_station_name", stationName);
                put("input_check_in_time", Integer.toString(checkInTime));

            }};

            result.add(failResult);
        }


        return result;

    }


}
