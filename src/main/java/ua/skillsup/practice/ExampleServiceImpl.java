package ua.skillsup.practice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.*;

public class ExampleServiceImpl implements ExampleService {
    private static final int MIN_TITLE_LENGTH = 3;
    private static final int MAX_TITLE_LENGTH = 20;
    private static final int MIN_PRICE = 15;

    private final ExampleDao exampleDao;
    private final IdGenerationService idGenerationService;

    public ExampleServiceImpl(ExampleDao exampleDao, IdGenerationService idGenerationService) {
        this.exampleDao = exampleDao;
        this.idGenerationService = idGenerationService;
    }

    @Override
    public void addNewItem(String title, BigDecimal price) {
        validateTitle(title);
        validatePrice(price);

        ExampleEntity entity = new ExampleEntity();
        entity.setId(idGenerationService.getNext());
        entity.setTitle(title);
        entity.setPrice(price.setScale(2, RoundingMode.HALF_UP));
        entity.setDateIn(LocalDateTime.now().toInstant(ZoneOffset.UTC));
        exampleDao.store(entity);
    }

    @Override
    public Map<LocalDate, BigDecimal> getStatistic() {
        LocalDate today = LocalDate.now();
        Map<LocalDate, List<BigDecimal>> allPricesByDate = exampleDao.findAll().stream()
                .collect(
                        groupingBy(
                                entity -> toLocalDate(entity.getDateIn()),
                                mapping(ExampleEntity::getPrice, toList())
                        )
                );

        return allPricesByDate.entrySet().stream()
                .filter(entry -> !entry.getKey().isEqual(today))
                .collect(
                        toMap(
                                Map.Entry::getKey,
                                entry -> getAveragePrice(entry.getValue())
                        )
                );
    }

    private LocalDate toLocalDate(Instant date) {
        return LocalDateTime.ofInstant(date, ZoneOffset.UTC).toLocalDate();
    }

    private void validateTitle(String title) {
        if (Objects.isNull(title) || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is mandatory");
        }

        if (title.length() < MIN_TITLE_LENGTH || title.length() > MAX_TITLE_LENGTH) {
            String errorMessage = String.format("Title length should be from %d to %d", MIN_TITLE_LENGTH, MIN_TITLE_LENGTH);
            throw new IllegalArgumentException(errorMessage);
        }

        Set<String> allTitles = exampleDao.findAll().stream()
                .map(ExampleEntity::getTitle)
                .collect(toSet());

        if (allTitles.contains(title)) {
            throw new IllegalArgumentException("Title is not unique");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (Objects.isNull(price)) {
            throw new IllegalArgumentException("Price is mandatory");
        }

        if (price.compareTo(new BigDecimal(MIN_PRICE)) < 0) {
            throw new IllegalArgumentException("Price is less than " + MIN_PRICE);
        }
    }

    private BigDecimal getAveragePrice(List<BigDecimal> prices) {
        BigDecimal sum = prices.stream()
                .map(Objects::requireNonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(prices.size()), RoundingMode.HALF_UP);
    }
}
