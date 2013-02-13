source("~/Dev/baidd/BaiddTest/analysis/load.r")
r<-readcsv("results.csv")
attach(r)

property <- PlayInformMoves
xlab1 <- "propose and reject"
xlab2 <- "propose and inform"

# Move count
boxplot(e_move ~ property, names=c(xlab1,xlab2), ylab="count")
means <- c(mean(e_move[property=="false"]),mean(e_move[property=="true"]))
points(means,pch=18,cex=2)

# Relevance
boxplot(e_relevance.strong ~ property, names=c(xlab1,xlab2), ylab="relevance")
means <- c(mean(e_relevance.strong[property=="false"]),mean(e_relevance.strong[property=="true"]))
points(means,pch=18,cex=2)

# Information concealment
boxplot(e_concealment ~ property, names=c(xlab1,xlab2), ylab="conceal")
means <- c(mean(e_concealment[property=="false"]),mean(e_concealment[property=="true"]))
points(means,pch=18,cex=2)

# Combined utility
boxplot(e_total.o ~ property, names=c(xlab1,xlab2), ylab="utility")
means <- c(mean(e_total.o[property=="false"]),mean(e_total.o[property=="true"]))
points(means,pch=18,cex=2)

# Pareto
plot(e_pareto.o ~ property, ylab="pareto")

# Baseline comparison
boxplot(e_total.avg, xlab="baseline", ylab="utility")
means <- c(mean(e_total.avg))
points(means,pch=18,cex=2)

boxplot(e_total.o ~ property, names=c(xlab1,xlab2), ylab="utility", boxwex=.4, at=c(0.9,2.1))
boxplot(e_total.avg, names=c("baseline"), at=c(1.5),add=TRUE)

