source("~/Dev/baidd/BaiddTest/analysis/load.r")
attach(r1)
attach(r2)
attach(r3)

plot(Strategy, e_move)
plot(Strategy, e_move, ylim=c(0, 150))
plot(Strategy, e_total.o)

boxplot(e_move ~ PlayOnlyRejects)
boxplot(e_move ~ Strategy)
boxplot(e_move ~ Distribution, notch=T, ylim=c(0, 150))

hist(e_move, 100)
hist(e_total.o)
hist(e_total.avg)

eb(FactNegation, e_move, "moves")
eb(File, e_move, "moves")

qqnorm(e_move)
qqnorm(e_total.o)

which(e_move > 100)

# Histogram per category
library(lattice)
tmp <- e_move[e_move < 400]
histogram(~ tmp | as.factor(Distribution), data=r1, breaks=c(0:250))

# Build analysis of variance
model <- aov(e_move ~ File)
summary(model)
table(Distribution)

# Windows
setwd("D:\\")
source("analysis\\load.r")

