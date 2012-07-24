//
//  MasterViewController.h
//  maven-ios-example-project
//
//  Created by Sebastian Bott on 24.07.12.
//  Copyright (c) 2012 let's dev. All rights reserved.
//

#import <UIKit/UIKit.h>

@class DetailViewController;

@interface MasterViewController : UITableViewController

@property (strong, nonatomic) DetailViewController *detailViewController;

@end
