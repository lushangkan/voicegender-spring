library(tuneR)
library(seewave)
library(fftw)

r <- tuneR::readWave(path, units = "seconds")

b<- c(0,22) #in case bp its higher than can be due to sampling rate
if(b[2] > ceiling(r@samp.rate/2000) - 1) b[2] <- ceiling(r@samp.rate/2000) - 1

#frequency spectrum analysis
songspec <- seewave::spec(r, f = r@samp.rate, plot = FALSE)
analysis <- seewave::specprop(songspec, f = r@samp.rate, flim = c(0, 280/1000), plot = FALSE)

#save parameters
meanfreq <- analysis$mean/1000
sd <- analysis$sd/1000
median <- analysis$median/1000
Q25 <- analysis$Q25/1000
Q75 <- analysis$Q75/1000
IQR <- analysis$IQR/1000
skew <- analysis$skewness
kurt <- analysis$kurtosis
sp_ent <- analysis$sh
sfm <- analysis$sfm
mode <- analysis$mode/1000
centroid <- analysis$cent/1000

#Frequency with amplitude peaks
peakf <- 0#seewave::fpeaks(songspec, f = r@samp.rate, wl = wl, nmax = 3, plot = FALSE)[1, 1]

#Fundamental frequency parameters
ff <- seewave::fund(r, f = r@samp.rate, ovlp = 50, threshold = 5, fmax = 280, ylim=c(0, 280/1000), plot = FALSE, wl = 512)[, 2]
meanfun<-mean(ff, na.rm = T)
minfun<-min(ff, na.rm = T)
maxfun<-max(ff, na.rm = T)

#Dominant frecuency parameters
y <- seewave::dfreq(r, f = r@samp.rate, wl = 512, ylim=c(0, 280/1000), ovlp = 0, plot = F, threshold = 5, bandpass = b * 1000, fftw = TRUE)[, 2]
meandom <- mean(y, na.rm = TRUE)
mindom <- min(y, na.rm = TRUE)
maxdom <- max(y, na.rm = TRUE)
dfrange <- (maxdom - mindom)

#modulation index calculation
changes <- vector()
for(j in which(!is.na(y))){
change <- abs(y[j] - y[j + 1])
changes <- append(changes, change)
}
if(mindom==maxdom) modindx<-0 else modindx <- mean(changes, na.rm = T)/dfrange

result <- c(meanfreq, sd, median, Q25, Q75, IQR, skew, kurt, sp_ent, sfm, mode,
            centroid, meanfun, minfun, maxfun, meandom, mindom, maxdom, dfrange, modindx)
