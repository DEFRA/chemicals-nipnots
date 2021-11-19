create database [reach-nipnots] COLLATE Latin1_General_100_CI_AI_SC
go
exec sp_configure 'contained database authentication', 1
go
reconfigure
go
alter database [reach-nipnots] set containment = partial
go
