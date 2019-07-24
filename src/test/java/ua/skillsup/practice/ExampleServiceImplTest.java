package ua.skillsup.practice;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExampleServiceImplTest {
    private static final int MIN_TITLE_LENGTH = 3;
    private static final int MAX_TITLE_LENGTH = 20;
    private static final int MIN_PRICE = 15;
    @Mock
    private ExampleDao exampleDao;
    @Mock
    private IdGenerationService idGenerationService;
    @Captor
    private ArgumentCaptor<ExampleEntity> exampleEntityArgumentCaptor = ArgumentCaptor.forClass(ExampleEntity.class);

    private ExampleService exampleService;

    @Before
    public void setUp() {
        exampleService = new ExampleServiceImpl(exampleDao, idGenerationService);
    }

    @DisplayName("Happy path case")
    @Test
    public void addNewItem() {
        //GIVEN
        final String title = "expected";
        final BigDecimal price = BigDecimal.valueOf(100);
        final Long id = 1L;

        ExampleEntity entity = new ExampleEntity();
        entity.setId(id);
        entity.setTitle(title);
        entity.setPrice(price.setScale(2, RoundingMode.HALF_UP));
        when(idGenerationService.getNext()).thenReturn(id);

        //WHEN
        exampleService.addNewItem(title, price);

        //THEN
        verify(exampleDao).store(exampleEntityArgumentCaptor.capture());
        ExampleEntity exampleEntity = exampleEntityArgumentCaptor.getValue();
        assertThat(exampleEntity).isEqualToIgnoringGivenFields(entity, "dateIn");
        LocalDate dateIn = LocalDateTime.ofInstant(exampleEntity.getDateIn(), ZoneId.ofOffset("UTC", ZoneOffset.UTC)).toLocalDate();
        assertThat(dateIn).isEqualTo(LocalDate.now());
    }

    @DisplayName("Fail on null name")
    @Test
    public void addNewItem__should_throw_IllegalArgumentException_when_title_is_null() {

        //GIVEN
        final BigDecimal price = BigDecimal.valueOf(100);

        //WHEN && THEN
        assertThatThrownBy(() -> exampleService.addNewItem(null, price))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title is mandatory");

    }

    @DisplayName("Fail on empty name")
    @Test
    public void addNewItem__should_throw_IllegalArgumentException_when_title_is_empty_value() {

        //GIVEN
        final BigDecimal price = BigDecimal.valueOf(100);
        final String title = "    ";

        //WHEN && THEN
        assertThatThrownBy(() -> exampleService.addNewItem(title, price))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title is mandatory");

    }

    @DisplayName("Fail on invalid min name")
    @Test
    public void addNewItem__should_throw_IllegalArgumentException_when_title_less_than_min_title_length() {

        //GIVEN
        final BigDecimal price = BigDecimal.valueOf(100);
        final String title = getStringWithLength(MIN_TITLE_LENGTH - 1);

        //WHEN && THEN
        assertThatThrownBy(() -> exampleService.addNewItem(title, price))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format("Title length should be from %d to %d", MIN_TITLE_LENGTH, MIN_TITLE_LENGTH));

    }

    @DisplayName("Fail on invalid max name")
    @Test
    public void addNewItem__should_throw_IllegalArgumentException_when_title_more_than_max_title_length() {

        //GIVEN
        final BigDecimal price = BigDecimal.valueOf(100);
        final String title = getStringWithLength(MAX_TITLE_LENGTH + 1);

        //WHEN
        assertThatThrownBy(() -> exampleService.addNewItem(title, price))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format("Title length should be from %d to %d", MIN_TITLE_LENGTH, MIN_TITLE_LENGTH));

    }

    @DisplayName("Fail on not unique name")
    @Test
    public void addNewItem__should_throw_IllegalArgumentException_when_title_not_unique() {

        //GIVEN
        final BigDecimal price = BigDecimal.valueOf(100);
        final String title = "expected";
        final Long id = 1L;
        final LocalDate date = LocalDate.of(2020, 1, 1);

        ExampleEntity entity = new ExampleEntity();
        entity.setId(id);
        entity.setTitle(title);
        entity.setPrice(price.setScale(2, RoundingMode.HALF_UP));
        entity.setDateIn(date.atStartOfDay().toInstant(ZoneOffset.UTC));
        when(idGenerationService.getNext()).thenReturn(id);

        //WHEN
        exampleService.addNewItem(title, price);
        when(exampleDao.findAll()).thenReturn(Collections.singletonList(entity));

        //THEN
        assertThatThrownBy(() -> exampleService.addNewItem(title, price))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title is not unique");

    }

    @DisplayName("Fail on empty price")
    @Test
    public void addNewItem__should_throw_IllegalArgumentException_when_price_is_null() {

        //GIVEN
        final String title = "expected";

        //WHEN
        assertThatThrownBy(() -> exampleService.addNewItem(title, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Price is mandatory");

    }

    @DisplayName("Fail on invalid min price")
    @Test
    public void addNewItem__should_throw_IllegalArgumentException_when_price_less_than_min_price() {

        //GIVEN
        final String title = "expected";
        final BigDecimal price = BigDecimal.valueOf(MIN_PRICE - 1);

        //WHEN
        assertThatThrownBy(() -> exampleService.addNewItem(title, price))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Price is less than " + MIN_PRICE);

    }


    @DisplayName("Happy path")
    @Test
    public void getStatistic() {
        //GIVEN
        final String title1 = "title1";
        final String title2 = "title2";
        final String title3 = "title3";
        final BigDecimal price1 = BigDecimal.valueOf(100);
        final BigDecimal price2 = BigDecimal.valueOf(100);
        final BigDecimal price3 = BigDecimal.valueOf(200);
        final Long id1 = 1L;
        final Long id2 = 2L;
        final Long id3 = 3L;

        final LocalDateTime date1 = LocalDateTime.now().minusDays(1);
        final LocalDateTime date2 = LocalDateTime.now().minusDays(1);
        final LocalDateTime today = LocalDateTime.now();

        ExampleEntity entity1 = new ExampleEntity();
        entity1.setId(id1);
        entity1.setTitle(title1);
        entity1.setPrice(price1.setScale(2, RoundingMode.HALF_UP));
        entity1.setDateIn(date1.toInstant(ZoneOffset.UTC));
        when(idGenerationService.getNext()).thenReturn(id1);

        ExampleEntity entity2 = new ExampleEntity();
        entity2.setId(id2);
        entity2.setTitle(title2);
        entity2.setPrice(price2.setScale(2, RoundingMode.HALF_UP));
        entity2.setDateIn(date2.toInstant(ZoneOffset.UTC));
        when(idGenerationService.getNext()).thenReturn(id2);

        ExampleEntity entity3 = new ExampleEntity();
        entity3.setId(id3);
        entity3.setTitle(title3);
        entity3.setPrice(price3.setScale(2, RoundingMode.HALF_UP));
        entity3.setDateIn(today.toInstant(ZoneOffset.UTC));
        when(idGenerationService.getNext()).thenReturn(id3);

        when(exampleDao.findAll()).thenReturn(Arrays.asList(entity1, entity2, entity3));

        Map<LocalDate, BigDecimal> expectedStatistics = new HashMap<LocalDate, BigDecimal>() {{
            put(date1.toLocalDate(), price1.setScale(2, RoundingMode.HALF_UP));
        }};


        //WHEN

        Map<LocalDate, BigDecimal> actualStatistics = exampleService.getStatistic();

        //THEN
        assertThat(actualStatistics).isEqualTo(expectedStatistics);

    }

    @DisplayName("Check empty statistic")
    @Test
    public void getStatistic_should_be_empty_when_contains_only_current_datein() {
        //GIVEN
        final String title1 = "title1";
        final BigDecimal price1 = BigDecimal.valueOf(200);
        final Long id1 = 1L;

        ExampleEntity entity1 = new ExampleEntity();
        entity1.setId(id1);
        entity1.setTitle(title1);
        entity1.setPrice(price1.setScale(2, RoundingMode.HALF_UP));
        entity1.setDateIn(LocalDateTime.now().toInstant(ZoneOffset.UTC));
        when(idGenerationService.getNext()).thenReturn(id1);

        when(exampleDao.findAll()).thenReturn(Arrays.asList(entity1));

        Map<LocalDate, BigDecimal> expectedStatistics = new HashMap<LocalDate, BigDecimal>() {{
        }};


        //WHEN

        Map<LocalDate, BigDecimal> actualStatistics = exampleService.getStatistic();

        //THEN
        assertThat(actualStatistics).isEqualTo(expectedStatistics);

    }

    private String getStringWithLength(int length) {
        return Stream.generate(() -> "a")
                .limit(length)
                .collect(joining());
    }
}