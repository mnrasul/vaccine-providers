# Vaccine Providers

How to run
``` mvn quarkus: dev```


Example postman

``` POST http://localhost:8080/vaccine-availability-query```

```json
{
    "inputObject": {
        "state": "AZ",
        "fruit": "Apple"
        },
    "outputObject": {}
}
```

Output
```json

[
    {
        "vendor": "BioN",
        "count": 20,
        "rule": "VaccineAvailability Rule 15"
    },
    {
        "vendor": "BioN",
        "count": 10,
        "rule": "VaccineAvailability Rule 16"
    },
    {
        "vendor": "BioN",
        "count": 25,
        "rule": "VaccineAvailability Rule 17"
    },
    {
        "vendor": "PFZ",
        "count": 10,
        "rule": "VaccineAvailability Rule 18"
    },
    {
        "vendor": "PFZ",
        "count": 20,
        "rule": "VaccineAvailability Rule 19"
    },
    {
        "vendor": "PFZ",
        "count": 25,
        "rule": "VaccineAvailability Rule 20"
    },
    {
        "vendor": "BioN",
        "count": 20,
        "rule": "VaccineAvailability Rule 21"
    }
]
```

Based on the rules in the sheet, `AZ` and `Apple` matches rules `15 -> 17`, as well as the more generic state wide ones ... `18 - 22`.

Is there a way to only return `15-17` since they are more specific.

If the question was `AZ` and `Pineapple`, it would be fine to return `18-22`, since they are the fallback rules.
