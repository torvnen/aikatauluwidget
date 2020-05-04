#TODO expand readme
- building app
- installation on Android
- development
- generating GraphQL schema

## Why use ContentProvider, and not broadcast/receive?
The official Android documentation suggests using it when data is meant to be exposed to Widgets.
In this project, the main advantage is that the responsibility of building the widget view(s) can be directly delegated to the RemoteViewsService implementation.