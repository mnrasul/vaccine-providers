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

# Toshiya Kobayashi submitted a branch with one possible way

`tkobayas-flag-based` has his suggested implementation.

Another option is to have an extra column with a notion of priority, and pick the highest one. I'll submit an example as a branch. The downside of the second approach is that it requires post processing. If one is interested in  leveraging the REST apis, that's likely a deal breaker. In my case, we are using AWS Lambda interface, so it is an equally acceptable solution.

Relevant zulip-chat thread: https://kie.zulipchat.com/#narrow/stream/232677-drools/topic/Match.20on.20the.20most.20strictest.20rules.20and.20not.20more.20general.20ones
